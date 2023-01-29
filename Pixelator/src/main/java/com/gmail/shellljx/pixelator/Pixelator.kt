package com.gmail.shellljx.pixelator

import android.graphics.Bitmap
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

    override fun setBrush(bitmap: Bitmap) {
        if (mId != 0L) {
            setBrush(mId, bitmap)
        }
    }

    override fun touchEvent(x: Float, y: Float) {
        if (mId != 0L) {
            touchEvent(mId, x, y)
        }
    }

    override fun refreshFrame() {
        if (mId != 0L) {
            refreshFrame(mId);
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
    private external fun setBrush(id: Long, bitmap: Bitmap): Boolean
    private external fun touchEvent(id: Long, x: Float, y: Float)
    private external fun refreshFrame(id: Long)

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
