package com.gmail.shellljx.pixelator

import android.graphics.Bitmap

interface IRenderListener {
    fun onEGLContextCreate()
    fun onEGLWindowCreate()
    fun onFrameAvaliable(x:Int, y:Int, width: Int, height: Int)
    fun onFrameSaved(bitmap: Bitmap)
}
