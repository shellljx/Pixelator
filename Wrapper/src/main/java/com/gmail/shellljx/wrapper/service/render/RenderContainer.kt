package com.gmail.shellljx.wrapper.service.render

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class RenderContainer : FrameLayout {
    private var mVideoRenderLayer: IRenderLayer? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    internal fun bindVideoRenderLayer(layer: IRenderLayer, marginBottom: Int) {
        mVideoRenderLayer = layer
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp.bottomMargin = marginBottom
        addView(layer.view(), 0, lp)
    }
}
