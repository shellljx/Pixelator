package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.gmail.shellljx.pixelate.*
import com.gmail.shellljx.pixelate.PixelatorFragment.Companion.KEY_IMAGE_DELEGATE
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.pixelate.view.CircleSeekbarView
import com.gmail.shellljx.pixelate.view.PickItem
import com.gmail.shellljx.pixelate.widget.WidgetEvents
import com.gmail.shellljx.pixelator.*
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.gesture.*
import com.gmail.shellljx.wrapper.service.panel.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.json.JSONObject

class EffectsPanel(context: Context) : AbsPanel(context), CircleSeekbarView.OnSeekPercentListener, OnTapObserver, OnSingleMoveObserver, UndoRedoStateObserver {
    override val tag: String
        get() = EffectsPanel::class.java.simpleName

    override val panelConfig: PanelConfig
        get() {
            val config = PanelConfig()
            config.exitAnim = R.anim.translate_fade_out
            config.enterAnim = R.anim.translate_fade_in
            return config
        }

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var mEffectService: IEffectService? = null
    private var mMaskService: IMaskLockService? = null
    private val mEffectsRecyclerView by lazy { getView()?.findViewById<RecyclerView>(R.id.rv_effects) }
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
    private var mPickPanelToken: PanelToken? = null
    private val mLockItems = arrayListOf(
        PickItem(-1, context.getString(R.string.lock_portrait)),
        PickItem(-1, context.getString(R.string.lock_background)),
        PickItem(-1, context.getString(R.string.lock_off))
    )
    private val mPaintItems = arrayListOf(
        PickItem(R.drawable.ic_graffiti, context.getString(R.string.paint_type_graffiti)),
        PickItem(R.drawable.ic_rect, context.getString(R.string.paint_type_rect)),
        PickItem(R.drawable.ic_circle, context.getString(R.string.paint_type_circle))
    )

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
        mEffectService = mContainer.getServiceManager().getService(EffectService::class.java)
        mMaskService = mContainer.getServiceManager().getService(MaskLockService::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_effects_layout
    }

    override fun onViewCreated(view: View?) {
        view ?: return
        mEffectsRecyclerView?.layoutManager = GridLayoutManager(context, 5)
        mEffectsRecyclerView?.adapter = mEffectsAdapter
        mEffectsRecyclerView?.addItemDecoration(GridSpacingItemDecoration(5, 10.dp(), true))
        mPointSeekbar?.setSeekPercentListener(this)
        val minSize = mContainer.getConfig().minPaintSize
        val maxSize = mContainer.getConfig().maxPaintSize
        val percent = mCoreService?.getPaintSize()?.let { (it - minSize) * 1f / (maxSize - minSize) }
            ?: 0f
        mPointSeekbar?.setPercent(percent)
        mContainer.getGestureService()?.addTapObserver(this)
        mContainer.getGestureService()?.addSingleMoveObserver(this)
        mCoreService?.addUndoRedoStateObserver(this)

        mPaintView?.setOnClickListener {
            val paintType = mCoreService?.getPaintType() ?: return@setOnClickListener
            mPickPanelToken = mContainer.getPanelService()?.showPanel(PickerPanel::class.java)?.apply {
                mContainer.getPanelService()?.updatePayload(this, PickerPanel.PickPayload(mPaintItems, getPaintTypePosition(paintType)) {
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
                    Toast.makeText(mContainer.getContext(), mPaintItems[it].text, Toast.LENGTH_SHORT).show()
                })
            }
        }
        mLockView?.setOnClickListener {
            val maskMode = mMaskService?.getMaskMode() ?: return@setOnClickListener
            mPickPanelToken = mContainer.getPanelService()?.showPanel(PickerPanel::class.java)?.apply {
                mContainer.getPanelService()?.updatePayload(this, PickerPanel.PickPayload(mLockItems, getMaskModePosition(maskMode)) { position ->
                    val isOn = position != 2
                    mLockView?.isSelected = isOn
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
            val type = if (it.isSelected) ERASER else PAINT
            mCoreService?.setPaintMode(type)
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
            mContainer.getDelegateService()?.getDelegate<IImageDelegate>(KEY_IMAGE_DELEGATE)?.openAlbum()
        }
    }

    override fun onAttach() {
        mEffectService?.let {
            setEffectItems(it.getEffects())
        }
    }

    fun setEffectItems(effectList: List<EffectItem>) {
        val startPosition = effectItems.size
        effectItems.addAll(effectList)
        mEffectsAdapter.notifyItemRangeInserted(startPosition, effectList.size)
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
            val holder = EffectHolder(LayoutInflater.from(context).inflate(R.layout.layout_effect_item, parent, false) as ViewGroup)
            holder.itemView.setOnClickListener {
                val effect = effectItems[holder.adapterPosition]
                val effectObj = JSONObject()
                effectObj.put("type", effect.type)
                val configObj = JSONObject()
                configObj.put("url", effect.url)
                effectObj.put("config", configObj)
                val effectStr = effectObj.toString()
                mCoreService?.setEffect(effectStr)
                Toast.makeText(mContainer.getContext(), "apply effect ${effect.id}", Toast.LENGTH_SHORT).show()
            }
            return holder
        }

        override fun getItemCount(): Int {
            return effectItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val effect = effectItems[position]
            if (holder is EffectHolder) {
                val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + effect.cover))
                requestBuilder.resizeOptions = ResizeOptions(200, 200)
                val controlBuilder = Fresco.newDraweeControllerBuilder()
                controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
                controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
                controlBuilder.oldController = holder.coverView.controller
                holder.coverView.controller = controlBuilder.build()
            }
        }
    }

    inner class EffectHolder(root: ViewGroup) : ViewHolder(root) {
        val coverView = itemView.findViewById<SimpleDraweeView>(R.id.iv_cover)
    }

    class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int, private val includeEdge: Boolean) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount
                outRect.right = (column + 1) * spacing / spanCount

                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right = spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
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
        mPointSeekbar?.visibility = View.VISIBLE
        mOperationArea?.visibility = View.VISIBLE
        return false
    }

    override fun onSeekStart() {
        mContainer.getControlService()?.sendWidgetMessage(WidgetEvents.MSG_SHOW_FINGER_POINT)
    }

    override fun onSeekPercent(percent: Float) {
        val size = mContainer.getConfig().run { minPaintSize + (maxPaintSize - minPaintSize) * percent }.toInt()
        mCoreService?.setPaintSize(size)
    }

    override fun onSeekComplete() {
        mContainer.getControlService()?.sendWidgetMessage(WidgetEvents.MSG_HIDE_FINGER_POINT)
    }

    override fun onUndoRedoStateChange(canUndo: Boolean, canRedo: Boolean) {
        mUndoView?.isSelected = canUndo
        mRedoView?.isSelected = canRedo
    }
}