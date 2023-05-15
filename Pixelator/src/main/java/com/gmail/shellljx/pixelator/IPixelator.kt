package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IPixelator {

    fun addImagePath(path: String)

    fun setBrush(bitmap: Bitmap)

    fun pushTouchBuffer(buffer: FloatArray)

    fun translate(scale: Float, angle: Float, translateX: Float, translateY: Float)

    fun refreshFrame()

    fun setRenderListener(listener: IRenderListener)
}
