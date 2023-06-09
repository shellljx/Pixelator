package com.gmail.shellljx.pixelator

import android.graphics.Bitmap
import android.view.Surface

interface IPixelator {

    fun setDisplaySurface(surface: Surface)

    fun viewPortChanged(width: Int, height: Int)

    fun destroyDisplaySurface()

    fun addImagePath(path: String, rotate: Int)

    fun setBrush(bitmap: Bitmap)

    fun setDeeplabMask(bitmap: Bitmap)

    fun setDeeplabMaskMode(@MaskMode mode: Int)

    fun setPaintType(@PaintType paintType: Int)

    fun setPaintSize(size: Int)

    fun pushTouchBuffer(buffer: FloatArray, cx: Float, cy: Float)

    fun stopTouch()

    fun setMatrix(matrix: FloatArray)

    fun refreshFrame()

    fun undo()

    fun redo()

    fun getMiniScreen(): IMiniScreen

    fun setRenderListener(listener: IRenderListener)
    fun save()
}
