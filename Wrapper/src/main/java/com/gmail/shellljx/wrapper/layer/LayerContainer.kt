package com.gmail.shellljx.wrapper.layer

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.render.IRenderLayer

class VELayerContainer : FrameLayout, ILayerContainer {
    companion object {
        private const val TAG = "VELayerContainer"
    }

    private lateinit var mContainer: IContainer
    private var mBuildInLayerMap = hashMapOf<BuildInLayer, AbsBuildInLayer>()
    private val mCustomLayers = arrayListOf<InnerCustomLayer>()
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
        mCustomLayers.forEach {
            if (it.alignType() == AlignType.ALIGN_LAYER_CONTAINER) {
                measureChildWithMargins(it.view(), widthMeasureSpec, 0, heightMeasureSpec, 0)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        mBuildInLayers.forEach {
            layoutChildren(it.getView(), left, top, right, bottom)
        }
        mCustomLayers.forEach {
            if (it.alignType() == AlignType.ALIGN_LAYER_CONTAINER) {
                layoutChildren(it.view(), left, top, right, bottom)
            }
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
        mCustomLayers.forEach {
            it.onViewportUpdate(offset)
        }
        mContainer.getRenderService()?.updateViewPort(offset)
    }

    override fun dispatchWindowInsets(insets: Rect) {
        mBuildInLayers.forEach {
            it.onWindowInsetsChanged(insets)
        }
        mCustomLayers.forEach {
            it.onWindowInsetsChanged(insets)
        }
    }

    override fun addCustomLayer(layer: ILayer, overBuildInLayer: BuildInLayer) {
        val buildInLayer = mBuildInLayerMap[overBuildInLayer] ?: return
        val insetIndex = buildInLayer.getIndex() + 1
        val customLayer = InnerCustomLayer(layer, overBuildInLayer)
        if (layer.alignType() == AlignType.ALIGN_RENDER_LAYER) {
            addView(customLayer.view(), insetIndex)
            mContainer.getRenderService()?.addRenderLayer(customLayer, false)
        } else {
            val layoutParams = customLayer.view().layoutParams
            if (layoutParams == null) {
                customLayer.view().layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            }
            addView(customLayer.view(), insetIndex)
        }
        mCustomLayers.add(customLayer)
        for (index in mBuildInLayers.indexOf(buildInLayer) + 1 until mBuildInLayers.size) {
            mBuildInLayers[index].increaseIndex()
        }
        dumpLayers()
    }

    override fun removeCustomLayer(layer: ILayer) {
        val iterator = mCustomLayers.iterator()
        while (iterator.hasNext()) {
            val customLayer = iterator.next()
            if (customLayer.getInnerLayer() == layer) {
                mContainer.getRenderService()?.removeRenderLayer(customLayer)
                iterator.remove()
                decreaseBuildInLayerIndex(customLayer.getOverBuildInLayer())
            }
        }
    }

    private fun decreaseBuildInLayerIndex(buildInLayer: BuildInLayer) {
        val layer = mBuildInLayerMap[buildInLayer] ?: return
        for (index in mBuildInLayers.indexOf(layer) + 1 until mBuildInLayers.size) {
            mBuildInLayers[index].decreaseIndex()
        }
    }

    private fun dumpLayers() {
        val dumpInfo = StringBuilder("\n")
        mBuildInLayers.forEach {
            dumpInfo.append(it.toString()).append("\n")
        }
        mCustomLayers.forEach {
            dumpInfo.append(it.toString()).append("\n")
        }
    }

    private class InnerCustomLayer(private val layer: ILayer, private val overBuildInLayer: BuildInLayer) : ILayer, IRenderLayer {
        override fun view(): View {
            return layer.view()
        }

        override fun level(): Int {
            return 0
        }

        override fun alignType(): AlignType {
            return layer.alignType()
        }

        override fun onViewportUpdate(offset: Int) {
            layer.onViewportUpdate(offset)
        }

        override fun onWindowInsetsChanged(insets: Rect) {
            layer.onWindowInsetsChanged(insets)
        }

        fun getInnerLayer(): ILayer {
            return layer
        }

        fun getOverBuildInLayer(): BuildInLayer {
            return overBuildInLayer
        }

        override fun toString(): String {
            return "[CustomLayer] $layer over $overBuildInLayer alignType: ${alignType()}"
        }
    }
}

interface ILayerContainer {
    fun init(veContainer: IContainer)
    fun getView(): View
    fun updateViewPort(offset: Int)
    fun dispatchWindowInsets(insets: Rect)
    fun addCustomLayer(layer: ILayer, overBuildInLayer: BuildInLayer)
    fun removeCustomLayer(layer: ILayer)
}

