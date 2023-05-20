package com.gmail.shellljx.pixelator

interface IRenderListener {
    fun onEGLContextCreate()
    fun onEGLWindowCreate()
    fun onFrameAvaliable(x:Int, y:Int, width: Int, height: Int)
}
