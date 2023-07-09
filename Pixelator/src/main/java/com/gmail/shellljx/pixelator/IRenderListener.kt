package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IRenderListener {
    fun onEGLContextCreate()
    fun onEGLWindowCreate()
    fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float, reset: Boolean)
    fun onInitBoundsChanged(left: Float, top: Float, right: Float, bottom: Float)
    fun onFrameSaved(bitmap: Bitmap)
    fun onRenderError(code: Int, msg: String)
    fun onUndoRedoChanged(canUndo: Boolean, canRedo: Boolean)
}
