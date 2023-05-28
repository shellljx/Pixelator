package com.gmail.shellljx.wrapper.service.render

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class RenderContainer : FrameLayout {
    var mVideoRenderLayer: IVideoRenderLayer? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child !is IVideoRenderLayer && mVideoRenderLayer != null) {
                mVideoRenderLayer?.view()?.let {
                    measureChildWithMargins(
                        child,
                        MeasureSpec.makeMeasureSpec(it.measuredWidth, MeasureSpec.EXACTLY),
                        0,
                        MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY),
                        0
                    )
                }
            } else {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val lp = child.layoutParams as LayoutParams
            val childLeft = left + paddingStart + lp.marginStart
            val childTop = top + paddingTop + lp.topMargin
            val width = child.measuredWidth
            val height = child.measuredHeight
            if (child is IVideoRenderLayer || mVideoRenderLayer == null) {
                child.layout(childLeft, childTop, childLeft + width, childTop + height)
            } else {
                mVideoRenderLayer?.view()?.let {
                    child.layout(it.left, it.top, it.left + it.measuredWidth, it.top + it.measuredHeight)
                }
            }
        }
    }

    internal fun bindVideoRenderLayer(layer: IVideoRenderLayer) {
        mVideoRenderLayer = layer
        addView(layer.view(), 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }
}
