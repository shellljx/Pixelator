package com.gmail.shellljx.pixelator

import android.view.Surface
import android.view.SurfaceHolder

class Pixelator private constructor() : IPixelator, SurfaceHolder.Callback {

    private var mId = 0L
    private var mRenderListener: IRenderListener? = null

    init {
        mId = create()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        if (mId != 0L) {
            onSurfaceCreate(mId, p0.surface)
        }
    }

    override fun addImagePath(path: String) {
        if (mId != 0L) {
            addImagePath(mId, path)
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mId != 0L) {
            onSurfaceChanged(mId, width, height)
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    private fun onEGLContextCreate() {
        mRenderListener?.onEGLContextCreate()
    }

    private fun onEGLWindowCreate() {
        mRenderListener?.onEGLWindowCreate()
    }

    override fun setRenderListener(listener: IRenderListener) {
        mRenderListener = listener
    }

    private external fun create(): Long
    private external fun onSurfaceCreate(id: Long, surface: Surface)
    private external fun onSurfaceChanged(id: Long, width: Int, height: Int)
    private external fun addImagePath(id: Long, path: String)

    companion object {
        // Used to load the 'pixelator' library on application startup.
        init {
            System.loadLibrary("pixelator")
        }

        fun create(): IPixelator {
            return Pixelator()
        }
    }
}
