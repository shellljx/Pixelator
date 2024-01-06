package com.gmail.shellljx.pixelate.panel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.gmail.shellljx.pixelate.*
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.extension.fill
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.pixelate.utils.FileUtils
import com.gmail.shellljx.pixelate.view.CircleSeekbarView
import com.gmail.shellljx.pixelate.view.PickItem
import com.gmail.shellljx.pixelate.viewmodel.EffectViewModel
import com.gmail.shellljx.pixelate.viewmodel.MainViewModel
import com.gmail.shellljx.pixelate.widget.WidgetEvents
import com.gmail.shellljx.pixelator.*
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.extension.activityViewModels
import com.gmail.shellljx.wrapper.extension.viewModels
import com.gmail.shellljx.wrapper.service.gesture.*
import com.gmail.shellljx.wrapper.service.panel.*
import com.google.android.material.bottomsheet.BottomSheetBehavior

@Keep
class MosaicPanel(context: Context) : AbsPanel(context), CircleSeekbarView.OnSeekPercentListener,
    OnTapObserver, OnSingleMoveObserver, UndoRedoStateObserver, PaintTypeObserver, OnImageObserver, MaskModeObserver {
    override val tag: String
        get() = MosaicPanel::class.java.simpleName

    override val panelConfig: PanelConfig
        get() {
            val config = PanelConfig()
            config.exitAnim = R.anim.translate_fade_out
            config.enterAnim = R.anim.translate_fade_in
            return config
        }

    private val mainViewModel: MainViewModel by activityViewModels()
    private val effectViewModel: EffectViewModel by viewModels()
    private var mCoreService: IPixelatorCoreService? = null
    private var mEffectService: IEffectService? = null
    private var mMaskService: IMaskLockService? = null

    @PaintType
    private var restorePaintType: Int = PaintType.Graffiti
    private lateinit var mEffectsRecyclerView: RecyclerView
    private val mOperationArea by lazy { getView()?.findViewById<ViewGroup>(R.id.operation_area) }
    private val mPointSeekbar by lazy { getView()?.findViewById<CircleSeekbarView>(R.id.point_seekbar) }
    private val mPaintView by lazy { getView()?.findViewById<ImageView>(R.id.iv_paint) }
    private val mLockView by lazy { getView()?.findViewById<View>(R.id.iv_lock) }
    private val mEraserView by lazy { getView()?.findViewById<View>(R.id.iv_eraser) }
    private val mUndoView by lazy { getView()?.findViewById<View>(R.id.iv_undo) }
    private val mRedoView by lazy { getView()?.findViewById<View>(R.id.iv_redo) }
    private val mAlbumView by lazy { getView()?.findViewById<View>(R.id.iv_album) }
    private val mBottomSheetView by lazy { getView()?.findViewById<View>(R.id.container) }
    private val mEffectsAdapter by lazy { EffectAdapter() }
    private val effectItems = arrayListOf<EffectItem>()
    private var selectedPosition: Int? = null
    private var mPickPanelToken: PanelToken? = null
    private val mLockItems = arrayListOf(
        PickItem(-1, context.getString(R.string.lock_portrait)),
        PickItem(-1, context.getString(R.string.lock_background)),
        PickItem(-1, context.getString(R.string.lock_off))
    )
    private val mPaintItems = arrayListOf(
        PickItem(R.drawable.ic_graffiti, context.getString(R.string.paint_type_graffiti)),
        PickItem(R.drawable.ic_rect, context.getString(R.string.paint_type_rect))
    )

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
        mEffectService = mContainer.getServiceManager().getService(EffectService::class.java)
        mMaskService = mContainer.getServiceManager().getService(MaskLockService::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_mosaic_layout
    }

    override fun onViewCreated(view: View?) {
        view ?: return
        mEffectsRecyclerView = view.findViewById(R.id.rv_effects)
        mEffectsRecyclerView.layoutManager = GridLayoutManager(context, 5)
        mEffectsRecyclerView.adapter = mEffectsAdapter
        mEffectsRecyclerView.addItemDecoration(GridSpacingItemDecoration(5, 5.dp()))
        mPointSeekbar?.setSeekPercentListener(this)
        val minSize = mContainer.getConfig().minPaintSize
        val maxSize = mContainer.getConfig().maxPaintSize
        val percent =
            mCoreService?.getPaintSize()?.let { (it - minSize) * 1f / (maxSize - minSize) }
                ?: 0f
        mPointSeekbar?.setPercent(percent)

        mPaintView?.isSelected = true
        mPaintView?.setOnClickListener {
            if (mPaintView?.isSelected != true) return@setOnClickListener
            val paintType = mCoreService?.getPaintType() ?: return@setOnClickListener
            mPickPanelToken =
                mContainer.getPanelService()?.showPanel(PickerPanel::class.java)?.apply {
                    mContainer.getPanelService()?.updatePayload(
                        this,
                        PickerPanel.PickPayload(mPaintItems, getPaintTypePosition(paintType)) {
                            val type = when (it) {
                                0 -> PaintType.Graffiti
                                1 -> PaintType.Rect
                                else -> PaintType.Circle
                            }
                            if (type == PaintType.Graffiti) {
                                mEffectService?.removDrawBox()
                            } else {
                                mEffectService?.addDrawBox()
                            }
                            mCoreService?.setPaintType(type)
                            mPaintView?.setImageResource(mPaintItems[it].icon)
                            Toast.makeText(
                                mContainer.getContext(),
                                mPaintItems[it].text,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
        }
        mLockView?.setOnClickListener {
            val maskMode = mCoreService?.getMaskMode() ?: return@setOnClickListener
            mPickPanelToken =
                mContainer.getPanelService()?.showPanel(PickerPanel::class.java)?.apply {
                    mContainer.getPanelService()?.updatePayload(
                        this,
                        PickerPanel.PickPayload(
                            mLockItems,
                            getMaskModePosition(maskMode)
                        ) { position ->
                            val mode = when (position) {
                                0 -> MaskMode.PERSON
                                1 -> MaskMode.BACKGROUND
                                else -> MaskMode.NONE
                            }
                            mMaskService?.setMaskMode(mode)
                        })
                }
        }
        mEraserView?.setOnClickListener {
            it.isSelected = !it.isSelected
            val mode = if (it.isSelected) ERASER else PAINT
            mCoreService?.setPaintMode(mode)
            if (it.isSelected) {
                savePaintType()
                mCoreService?.setPaintType(PaintType.Graffiti)
            } else {
                restorePaintType()
            }
            mPaintView?.isSelected = !it.isSelected
            val id = if (it.isSelected) R.string.paint_mode_eraser else R.string.paint_mode_paint
            Toast.makeText(context, context.getString(id), Toast.LENGTH_SHORT).show()
        }
        mUndoView?.setOnClickListener {
            mCoreService?.undo()
        }
        mRedoView?.setOnClickListener {
            mCoreService?.redo()
        }
        mAlbumView?.setOnClickListener {
            mainViewModel.openAlbumLiveData.postValue(0)
        }
        effectViewModel.effectsLiveData.observe(this) {
            setEffectItems(it)
        }
        effectViewModel.downloadLiveData.observe(this) {
            if (it.position !in effectItems.indices) return@observe
            val effect = effectItems[it.position]
            if (it.status == STATUS.Downloaded) {
                effect.fill()
                if (effect.status == STATUS.Downloaded) {
                    mEffectService?.applyEffect(effect)
                }
            } else if (effect.status != STATUS.NotDownload) {
                effect.status = STATUS.NotDownload
            }
            mEffectsAdapter.notifyItemChanged(it.position, 0)
        }
    }

    override fun onAttach() {
        mContainer.getGestureService()?.addTapObserver(this)
        mContainer.getGestureService()?.addSingleMoveObserver(this)
        mCoreService?.addUndoRedoStateObserver(this)
        mCoreService?.addPaintTypeObserver(this)
        mCoreService?.addImageObserver(this)
        mCoreService?.addMaskModeObserver(this)
    }

    override fun onImageLoaded(path: String) {
        mBottomSheetView?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onPaintTypeChanged(type: Int) {
        mPointSeekbar?.isVisible = type == PaintType.Graffiti
    }

    private fun savePaintType() {
        restorePaintType = mCoreService?.getPaintType() ?: PaintType.Graffiti
        mEffectService?.removDrawBox()
    }

    private fun restorePaintType() {
        mCoreService?.setPaintType(restorePaintType)
        if (restorePaintType == PaintType.Rect) {
            mEffectService?.addDrawBox()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setEffectItems(effectList: List<EffectItem>) {
        effectItems.clear()
        effectItems.addAll(effectList)
        mEffectsAdapter.notifyDataSetChanged()
    }

    private fun getMaskModePosition(@MaskMode maskMode: Int): Int {
        return when (maskMode) {
            MaskMode.PERSON -> 0
            MaskMode.BACKGROUND -> 1
            else -> 2
        }
    }

    private fun getPaintTypePosition(@PaintType type: Int): Int {
        return when (type) {
            PaintType.Graffiti -> 0
            PaintType.Rect -> 1
            else -> 2
        }
    }

    inner class EffectAdapter : Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val holder = EffectHolder(
                LayoutInflater.from(context)
                    .inflate(R.layout.layout_effect_item, parent, false) as ViewGroup
            )
            holder.itemView.setOnClickListener {
                val position = holder.adapterPosition
                val effect = effectItems[position]
                selectedPosition?.let { notifyItemChanged(it, 1) }
                selectedPosition = position
                notifyItemChanged(position, 1)
                if (effect.path == null && effect.type == EffectType.TypeImage) {
                    if (effect.status != STATUS.Downloading) {
                        effect.status = STATUS.Downloading
                        notifyItemChanged(position, 0)
                        effectViewModel.downloadEffect(position, effect.url, FileUtils.getEffectDir())
                    }
                } else {
                    mEffectService?.applyEffect(effect)
                }
            }
            return holder
        }

        override fun getItemCount(): Int {
            return effectItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position)
            } else {
                val effect = effectItems[position]
                if (holder is EffectHolder) {
                    holder.downloadView.isVisible = effect.status == STATUS.NotDownload && effect.type == EffectType.TypeImage
                    holder.progressView.isVisible = effect.status == STATUS.Downloading && effect.type == EffectType.TypeImage
                    holder.itemView.isSelected = position == selectedPosition
                }
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val effect = effectItems[position]
            if (holder is EffectHolder) {
                val requestBuilder =
                    ImageRequestBuilder.newBuilderWithSource(Uri.parse(effect.cover))
                requestBuilder.resizeOptions = ResizeOptions(200, 200)
                val controlBuilder = Fresco.newDraweeControllerBuilder()
                controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
                controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
                controlBuilder.oldController = holder.coverView.controller
                holder.coverView.controller = controlBuilder.build()
                holder.downloadView.isVisible = effect.status == STATUS.NotDownload && effect.type == EffectType.TypeImage
                holder.progressView.isVisible = effect.status == STATUS.Downloading && effect.type == EffectType.TypeImage
            }
        }
    }

    inner class EffectHolder(root: ViewGroup) : ViewHolder(root) {
        init {
            val size = (mEffectsRecyclerView.width - 25.dp()) / 5
            val lp = itemView.layoutParams
            lp?.height = size
            itemView.layoutParams = lp
        }

        val coverView = itemView.findViewById<SimpleDraweeView>(R.id.iv_cover)
        val downloadView = itemView.findViewById<View>(R.id.iv_download)
        val progressView = itemView.findViewById<View>(R.id.progress)
    }

    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int
    ) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount
            outRect.bottom = spacing

        }
    }

    override fun onStartSingleMove(): Boolean {
        mPointSeekbar?.visibility = View.INVISIBLE
        mOperationArea?.visibility = View.INVISIBLE
        return false
    }

    override fun onSingleDown(event: MotionEvent): Boolean {
        mBottomSheetView?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        if (mCoreService?.getPaintType() == PaintType.Graffiti) {
            mPointSeekbar?.visibility = View.VISIBLE
        }
        mOperationArea?.visibility = View.VISIBLE
        return false
    }

    override fun onSeekStart() {
        mContainer.getControlService()?.sendWidgetMessage(WidgetEvents.MSG_SHOW_FINGER_POINT)
    }

    override fun onSeekPercent(percent: Float) {
        val size =
            mContainer.getConfig().run { minPaintSize + (maxPaintSize - minPaintSize) * percent }
                .toInt()
        mCoreService?.setPaintSize(size)
    }

    override fun onSeekComplete() {
        mContainer.getControlService()?.sendWidgetMessage(WidgetEvents.MSG_HIDE_FINGER_POINT)
    }

    override fun onUndoRedoStateChange(canUndo: Boolean, canRedo: Boolean) {
        mUndoView?.isSelected = canUndo
        mRedoView?.isSelected = canRedo
    }

    override fun onMaskModeChanged(mode: Int) {
        mLockView?.isSelected = mode != MaskMode.NONE
    }
}