package com.gmail.shellljx.wrapper.service.render

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View

class SurfaceVideoRenderLayer : SurfaceView, IVideoRenderLayer {
    private val mAlignViews = arrayListOf<View>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mAlignViews.forEach {
            it.measure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        mAlignViews.forEach {
            it.layout(left, top, right, bottom)
        }
    }

    override fun addAlignLayer(layer: IRenderLayer) {
        if (!mAlignViews.contains(layer.view())) {
            mAlignViews.add(layer.view())
        }
    }

    override fun removeAlignLayer(layer: IRenderLayer) {
        val render = layer.view()
        if (mAlignViews.contains(render)) {
            mAlignViews.remove(render)
        }
    }

    override fun view(): View {
        return this
    }

    override fun level(): Int {
        return 0
    }
}
