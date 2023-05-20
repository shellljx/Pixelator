package com.gmail.shellljx.pixelator

interface IImageListener {
    /**
     * 图片被加载并且创建好纹理之后的回调
     * @param width image frame width
     * @param height image frame height
     */
    fun onFrameAvaliable(width: Int, height: Int)
}