package com.gmail.shellljx.pixelator

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.Surface
import androidx.annotation.Keep

@Keep
class Pixelator private constructor() : IPixelator {

    private var mId = 0L
    private var mRenderListener: IRenderListener? = null
    private val mMiniScreen: IMiniScreen
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        mId = create()
        mMiniScreen = MiniScreen(mId)
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
        if (mId != 0L) {
            onSurfaceDestroy(mId)
        }
    }

    override fun addImagePath(path: String, rotate: Int) {
        if (mId != 0L) {
            nativeAddImagePath(mId, path, rotate)
        }
    }

    override fun setEffect(config: String) {
        if (mId != 0L) {
            nativeSetEffect(mId, config)
        }
    }

    override fun updateEffect(config: String) {

    }

    override fun setBrush(bitmap: Bitmap) {
        if (mId != 0L) {
            setBrush(mId, bitmap)
        }
    }

    override fun setDeeplabMask(bitmap: Bitmap) {
        if (mId != 0L) {
            nativeSetDeeplabMask(mId, bitmap)
        }
    }

    override fun setDeeplabMaskMode(mode: Int) {
        if (mId != 0L) {
            nativeSetDeeplabMaskMode(mId, mode)
        }
    }

    override fun setPaintMode(paintMode: Int) {
        if (mId != 0L) {
            nativeSetPaintMode(mId, paintMode)
        }
    }

    override fun setPaintType(paintType: Int) {
        if (mId != 0L) {
            nativeSetPaintType(mId, paintType)
        }
    }

    override fun setPaintSize(size: Int) {
        if (mId != 0L) {
            setPaintSize(mId, size)
        }
    }

    override fun setCanvasHide(hide: Boolean) {
        if (mId != 0L) {
            setCanvasHide(mId, hide)
        }
    }

    override fun pushTouchBuffer(buffer: FloatArray, cx: Float, cy: Float) {
        if (buffer.isEmpty()) return
        if (mId != 0L) {
            pushTouchBuffer(mId, buffer, cx, cy)
        }
    }

    override fun startTouch(x: Float, y: Float) {
        if (mId != 0L) {
            nativeStartTouch(mId, x, y)
        }
    }

    override fun stopTouch() {
        if (mId != 0L) {
            nativeStopTouch(mId)
        }
    }

    override fun setMatrix(matrix: FloatArray) {
        if (mId != 0L) {
            nativeSetMatrix(mId, matrix)
        }
    }

    override fun updateViewPort(offset: Int) {
        if (mId != 0L) {
            nativeUpdateViewPort(mId, offset)
        }
    }

    override fun refreshFrame() {
        if (mId != 0L) {
            refreshFrame(mId)
        }
    }

    override fun undo() {
        if (mId != 0L) {
            nativeUndo(mId)
        }
    }

    override fun redo() {
        if (mId != 0L) {
            nativeRedo(mId)
        }
    }

    override fun getMiniScreen(): IMiniScreen {
        return mMiniScreen
    }

    override fun save(path: String) {
        if (mId != 0L) {
            nativeSave(mId, path)
        }
    }

    override fun destroy() {
        if (mId != 0L) {
            nativeDestroy(mId)
        }
    }

    /**
     * jni callback method
     */
    private fun onEGLContextCreate() {
        mainHandler.post {
            mRenderListener?.onEGLContextCreate()
        }
    }

    /**
     * jni callback method
     */
    private fun onEGLWindowCreate() {
        mainHandler.post {
            mRenderListener?.onEGLWindowCreate()
        }
    }

    /**
     * jni callback method
     */
    private fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float, reset: Boolean) {
        mainHandler.post {
            mRenderListener?.onFrameBoundsChanged(left, top, right, bottom, reset)
        }
    }

    private fun onInitBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
        mainHandler.post {
            mRenderListener?.onInitBoundsChanged(left, top, right, bottom)
        }
    }

    private fun onFrameSaved(bitmap: Bitmap) {
        mainHandler.post {
            mRenderListener?.onFrameSaved(bitmap)
        }
    }

    private fun onSaveSuccess(path: String) {
        System.out.println("lijinxiang $path")
        mainHandler.post {
            mRenderListener?.onSaveSuccess(path)
        }
    }

    private fun onRenderError(code: Int, msg: String) {
        mainHandler.post {
            mRenderListener?.onRenderError(code, msg)
        }
    }

    private fun onUndoRedoChanged(canUndo: Boolean, canRedo: Boolean) {
        mainHandler.post {
            mRenderListener?.onUndoRedoChanged(canUndo, canRedo)
        }
    }

    override fun setRenderListener(listener: IRenderListener) {
        mRenderListener = listener
    }

    private external fun create(): Long
    private external fun onSurfaceCreate(id: Long, surface: Surface)
    private external fun onSurfaceChanged(id: Long, width: Int, height: Int)
    private external fun onSurfaceDestroy(id: Long)
    private external fun nativeAddImagePath(id: Long, path: String, rotate: Int)
    private external fun nativeSetEffect(id: Long, config: String)
    private external fun nativeUpdateEffect(id: Long, config: String)
    private external fun setBrush(id: Long, bitmap: Bitmap): Boolean
    private external fun nativeSetDeeplabMask(id: Long, bitmap: Bitmap)
    private external fun nativeSetDeeplabMaskMode(id: Long, mode: Int)
    private external fun nativeSetPaintMode(id: Long, mode: Int)
    private external fun nativeSetPaintType(id: Long, type: Int)
    private external fun setPaintSize(id: Long, size: Int)
    private external fun setCanvasHide(id: Long, hide: Boolean)
    private external fun pushTouchBuffer(id: Long, floatArray: FloatArray, cx: Float, cy: Float)
    private external fun nativeStartTouch(id: Long, x: Float, y: Float)
    private external fun nativeStopTouch(id: Long)
    private external fun nativeSetMatrix(id: Long, floatArray: FloatArray)
    private external fun nativeUpdateViewPort(id: Long, offset: Int)
    private external fun refreshFrame(id: Long)
    private external fun nativeUndo(id: Long)
    private external fun nativeRedo(id: Long)
    private external fun nativeSave(id: Long, path: String)
    private external fun nativeDestroy(id: Long)

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
