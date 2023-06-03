package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.graphics.Rect
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.gmail.shellljx.pixelate.*
import com.gmail.shellljx.pixelate.service.IPixelatorCoreService
import com.gmail.shellljx.pixelate.service.PixelatorCoreService
import com.gmail.shellljx.pixelate.view.CircleSeekbarView
import com.gmail.shellljx.pixelate.widget.WidgetEvents
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.gesture.OnSingleDownObserver
import com.gmail.shellljx.wrapper.service.gesture.OnSingleUpObserver
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
import java.util.ArrayList

class EffectsPanel(context: Context) : AbsPanel(context), CircleSeekbarView.OnSeekPercentListener, OnSingleDownObserver, OnSingleUpObserver {
    override val tag: String
        get() = EffectsPanel::class.java.simpleName

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private val mEffectsRecyclerView by lazy { getView()?.findViewById<RecyclerView>(R.id.rv_effects) }
    private val mOperationArea by lazy { getView()?.findViewById<ViewGroup>(R.id.operation_area) }
    private val mPointSeekbar by lazy { getView()?.findViewById<CircleSeekbarView>(R.id.point_seekbar) }
    private val mUndoView by lazy { getView()?.findViewById<View>(R.id.iv_undo) }
    private val mRedoView by lazy { getView()?.findViewById<View>(R.id.iv_redo) }
    private val mEffectsAdapter by lazy { EffectAdapter() }
    private val effectItems = arrayListOf<EffectItem>()

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_effects_layout
    }

    override fun onViewCreated(view: View?) {
        mEffectsRecyclerView?.layoutManager = GridLayoutManager(context, 5)
        mEffectsRecyclerView?.adapter = mEffectsAdapter
        mEffectsRecyclerView?.addItemDecoration(GridSpacingItemDecoration(5, 15, true))
        mPointSeekbar?.setSeekPercentListener(this)
        val minSize = mContainer.getConfig().minPaintSize
        val maxSize = mContainer.getConfig().maxPaintSize
        val percent = mCoreService?.getPaintSize()?.let { (it - minSize) * 1f / (maxSize - minSize) } ?: 0f
        mPointSeekbar?.setPercent(percent)
        mContainer.getGestureService()?.addSingleUpObserver(this)
        mContainer.getGestureService()?.addSingleDownObserver(this)

        mUndoView?.setOnClickListener {

        }
        mRedoView?.setOnClickListener {

        }
    }

    fun setEffectItems(effectList: ArrayList<EffectItem>) {
        val startPosition = effectItems.size
        effectItems.addAll(effectList)
        mEffectsAdapter.notifyItemRangeInserted(startPosition, effectList.size)
    }

    inner class EffectAdapter : Adapter<ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return EffectHolder(LayoutInflater.from(context).inflate(R.layout.layout_effect_item, parent, false) as ViewGroup)
        }

        override fun getItemCount(): Int {
            return effectItems.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }
    }

    inner class EffectHolder(root: ViewGroup) : ViewHolder(root) {

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

    override fun onSingleDown(event: MotionEvent): Boolean {
        mPointSeekbar?.visibility = View.INVISIBLE
        mOperationArea?.visibility = View.INVISIBLE
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

}