package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IPixelator {

    fun addImagePath(path: String, listener: IImageListener? = null)

    fun setBrush(bitmap: Bitmap)

    fun pushTouchBuffer(buffer: FloatArray)

    fun translate(scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float)

    fun setMatrix(matrix: FloatArray)

    fun refreshFrame()

    fun setRenderListener(listener: IRenderListener)
}
