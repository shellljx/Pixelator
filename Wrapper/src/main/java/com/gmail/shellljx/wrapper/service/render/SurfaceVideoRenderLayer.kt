package com.gmail.shellljx.wrapper.service.render

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import com.gmail.shellljx.wrapper.IRenderContext

class SurfaceVideoRenderLayer : SurfaceView, IVideoRenderLayer, SurfaceHolder.Callback {
    private var mRenderContext: IRenderContext? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    init {
        holder.addCallback(this)
    }

    override fun bindRenderContext(renderContext: IRenderContext) {
        mRenderContext = renderContext
    }

    override fun view(): View {
        return this
    }

    override fun level(): Int {
        return 0
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mRenderContext?.setDisplaySuerface(holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        mRenderContext?.updateSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mRenderContext?.destroyDisplaySurface()
    }
}
