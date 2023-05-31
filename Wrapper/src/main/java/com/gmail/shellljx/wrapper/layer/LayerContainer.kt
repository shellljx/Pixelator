package com.gmail.shellljx.wrapper.layer

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.gmail.shellljx.wrapper.IContainer

class VELayerContainer : FrameLayout, ILayerContainer {
    companion object {
        private const val TAG = "VELayerContainer"
    }

    private lateinit var mContainer: IContainer
    private var mBuildInLayerMap = hashMapOf<BuildInLayer, AbsBuildInLayer>()
    private val mBuildInLayers = arrayListOf<AbsBuildInLayer>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, def: Int) : super(context, attributeSet, def) {
        setBackgroundColor(Color.BLACK)
    }

    override fun init(veContainer: IContainer) {
        mContainer = veContainer
        //gesture layer
        val gestureLayer = GestureLayer(veContainer, BuildInLayer.LayerGesture.index)
        mBuildInLayerMap[BuildInLayer.LayerGesture] = gestureLayer
        gestureLayer.attach(this)
        mBuildInLayers.add(gestureLayer)
        //render layer
        val renderLayer = RenderLayer(veContainer, BuildInLayer.LayerRender.index)
        renderLayer.attach(this)
        mBuildInLayerMap[BuildInLayer.LayerRender] = renderLayer
        mBuildInLayers.add(renderLayer)
        //control layer
        val controlLayer = ControlLayer(veContainer, BuildInLayer.LayerControl.index)
        mBuildInLayerMap[BuildInLayer.LayerControl] = controlLayer
        controlLayer.attach(this)
        mBuildInLayers.add(controlLayer)
        //panel layer
        val panelLayer = PanelLayer(mContainer, BuildInLayer.LayerPanel.index)
        mBuildInLayerMap[BuildInLayer.LayerPanel] = panelLayer
        panelLayer.attach(this)
        mBuildInLayers.add(panelLayer)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        mBuildInLayers.forEach {
            measureChildWithMargins(it.getView(), widthMeasureSpec, 0, heightMeasureSpec, 0)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mBuildInLayers.forEach {
            layoutChildren(it.getView(), left, top, right, bottom)
        }
    }

    private fun layoutChildren(child: View?, left: Int, top: Int, right: Int, bottom: Int) {
        child ?: return
        val parentRight: Int = right - left
        val parentBottom: Int = bottom - top
        if (child.visibility != GONE) {
            val lp = child.layoutParams as LayoutParams
            val width = child.measuredWidth
            val height = child.measuredHeight
            val childLeft: Int
            val childTop: Int
            var gravity = lp.gravity
            if (gravity == -1) {
                gravity = Gravity.TOP or Gravity.START
            }
            val layoutDirection = layoutDirection
            val absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection)
            val verticalGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
            childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL -> (parentRight - width) / 2 +
                        lp.leftMargin - lp.rightMargin
                Gravity.RIGHT -> {
                    lp.leftMargin
                }
                Gravity.LEFT -> lp.leftMargin
                else -> lp.leftMargin
            }
            childTop = when (verticalGravity) {
                Gravity.TOP -> lp.topMargin
                Gravity.CENTER_VERTICAL -> (parentBottom - height) / 2 +
                        lp.topMargin - lp.bottomMargin
                Gravity.BOTTOM -> parentBottom - height - lp.bottomMargin
                else -> lp.topMargin
            }
            child.layout(childLeft, childTop, childLeft + width, childTop + height)
        }
    }

    override fun getView(): View {
        return this
    }

    override fun updateViewPort(offset: Int) {
        mContainer.getRenderService()?.updateViewPort(offset)
    }

    override fun dispatchWindowInsets(insets: Rect) {
        mBuildInLayers.forEach {
            it.onWindowInsetsChanged(insets)
        }
    }
}

interface ILayerContainer {
    fun init(veContainer: IContainer)
    fun getView(): View
    fun updateViewPort(offset: Int)
    fun dispatchWindowInsets(insets: Rect)
}

