package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IPixelator {

    fun addImagePath(path: String)

    fun setBrush(bitmap: Bitmap)

    fun touchEvent(x: Float, y: Float)

    fun refreshFrame()

    fun setRenderListener(listener: IRenderListener)
}
