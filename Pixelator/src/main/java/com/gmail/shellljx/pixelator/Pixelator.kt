package com.gmail.shellljx.pixelator

import android.graphics.Bitmap
import android.view.Surface
import android.view.SurfaceHolder

class Pixelator private constructor() : IPixelator {

    private var mId = 0L
    private var mRenderListener: IRenderListener? = null

    init {
        mId = create()
    }

    private val miniScreen = object : IMiniScreen {
        override fun onSurfaceCreated(surface: Surface) {
            if (mId != 0L) {
                onMiniScreenSurfaceCreate(mId, surface)
            }
        }

        override fun onSurfaceChanged(width: Int, height: Int) {
            if (mId != 0L) {
                onMiniScreenSurfaceChanged(mId, width, height)
            }
        }

        override fun onSurfaceDestroy() {
            if (mId != 0L) {
                onMiniScreenSurfaceDestroy(mId)
            }
        }
    }

    override fun setDisplaySurface(surface: Surface) {
        if (mId != 0L) {
            onSurfaceCreate(mId, surface)
        }
    }

    override fun viewPortChanged(width: Int, height: Int) {
        if (mId != 0L) {
            onSurfaceChanged(mId, width, height)
        }
    }

    override fun destroyDisplaySurface() {

    }

    override fun addImagePath(path: String, rotate: Int) {
        if (mId != 0L) {
            nativeAddImagePath(mId, path, rotate)
        }
    }

    override fun setBrush(bitmap: Bitmap) {
        if (mId != 0L) {
            setBrush(mId, bitmap)
        }
    }

    override fun pushTouchBuffer(buffer: FloatArray, cx: Float, cy: Float) {
        if (buffer.isEmpty()) return
        if (mId != 0L) {
            pushTouchBuffer(mId, buffer, cx, cy)
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

    override fun getMiniScreen(): IMiniScreen {
        return miniScreen
    }

    override fun save() {
        if (mId != 0L) {
            nativeSave(mId)
        }
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
    private fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
        mRenderListener?.onFrameBoundsChanged(left, top, right, bottom)
    }

    private fun onFrameSaved(bitmap: Bitmap) {
        mRenderListener?.onFrameSaved(bitmap)
    }

    override fun setRenderListener(listener: IRenderListener) {
        mRenderListener = listener
    }

    private external fun create(): Long
    private external fun onSurfaceCreate(id: Long, surface: Surface)
    private external fun onSurfaceChanged(id: Long, width: Int, height: Int)
    private external fun onMiniScreenSurfaceCreate(id: Long, surface: Surface)
    private external fun onMiniScreenSurfaceChanged(id: Long, width: Int, height: Int)
    private external fun onMiniScreenSurfaceDestroy(id: Long)
    private external fun nativeAddImagePath(id: Long, path: String, rotate: Int)
    private external fun setBrush(id: Long, bitmap: Bitmap): Boolean
    private external fun pushTouchBuffer(id: Long, floatArray: FloatArray, cx: Float, cy: Float)
    private external fun nativeSetMatrix(id: Long, floatArray: FloatArray)
    private external fun refreshFrame(id: Long)
    private external fun nativeSave(id: Long)

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
