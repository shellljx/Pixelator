package com.gmail.shellljx.pixelator

interface IPixelator {

    fun addImagePath(path: String)

    fun touchEvent(x: Float, y: Float)

    fun refreshFrame()

    fun setRenderListener(listener: IRenderListener)
}
