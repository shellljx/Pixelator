package com.gmail.shellljx.pixelator

interface IPixelator {

    fun addImagePath(path: String)

    fun setRenderListener(listener: IRenderListener)
}
