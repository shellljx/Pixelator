package com.gmail.shellljx.pixelator

import android.graphics.Bitmap
import android.view.Surface
import android.view.SurfaceHolder

class Pixelator private constructor() : IPixelator, SurfaceHolder.Callback {

    private var mId = 0L
    private var mRenderListener: IRenderListener? = null
    var width = 0
    var height = 0

    init {
        mId = create()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        if (mId != 0L) {
            onSurfaceCreate(mId, p0.surface)
        }
    }

    override fun addImagePath(path: String, listener: IImageListener?) {
        if (mId != 0L) {
            nativeAddImagePath(mId, path, listener)
        }
    }

    override fun setBrush(bitmap: Bitmap) {
        if (mId != 0L) {
            setBrush(mId, bitmap)
        }
    }

    override fun pushTouchBuffer(buffer: FloatArray) {
        if (buffer.isEmpty()) return
        if (mId != 0L) {
            pushTouchBuffer(mId, buffer, buffer.size)
        }
    }

    override fun translate(scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float) {
        if (mId != 0L) {
            translate(mId, scale, pivotX, pivotY, angle, translateX, translateY)
        }
    }

    override fun setMatrix(matrix: FloatArray) {
        if (mId != 0L) {
            nativeSetMatrix(mId, matrix)
        }
    }

    override fun refreshFrame() {
        if (mId != 0L) {
            refreshFrame(mId);
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mId != 0L) {
            this.width = width
            this.height = height
            onSurfaceChanged(mId, width, height)
        }
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    /**
     * jni callback method
     */
    private fun onEGLContextCreate() {
        mRenderListener?.onEGLContextCreate()
    }

    /**
     * jni callback method
     */
    private fun onEGLWindowCreate() {
        mRenderListener?.onEGLWindowCreate()
    }

    /**
     * jni callback method
     */
    private fun onFrameAvaliable(x:Int, y:Int, width: Int, height: Int) {
        mRenderListener?.onFrameAvaliable(x, y, width, height)
    }

    override fun setRenderListener(listener: IRenderListener) {
        mRenderListener = listener
    }

    private external fun create(): Long
    private external fun onSurfaceCreate(id: Long, surface: Surface)
    private external fun onSurfaceChanged(id: Long, width: Int, height: Int)
    private external fun nativeAddImagePath(id: Long, path: String, listener: IImageListener?)
    private external fun setBrush(id: Long, bitmap: Bitmap): Boolean
    private external fun pushTouchBuffer(id: Long, floatArray: FloatArray, count: Int)
    private external fun translate(id: Long, scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float)
    private external fun nativeSetMatrix(id: Long, floatArray: FloatArray)
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
