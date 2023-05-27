package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IPixelator {

    fun addImagePath(path: String, rotate: Int)

    fun setBrush(bitmap: Bitmap)

    fun pushTouchBuffer(buffer: FloatArray)

    fun setMatrix(matrix: FloatArray)

    fun refreshFrame()

    fun getMiniScreen(): IMiniScreen

    fun setRenderListener(listener: IRenderListener)
    fun save()
}
