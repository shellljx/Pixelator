package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IPixelator {

    fun addImagePath(path: String)

    fun setBrush(bitmap: Bitmap)

    fun pushTouchBuffer(buffer: FloatArray)

    fun refreshFrame()

    fun setRenderListener(listener: IRenderListener)
}
