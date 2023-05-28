package com.gmail.shellljx.wrapper

interface IService {
    /**
     * service 被启动的时候调用
     */
    fun onStart()

    /**
     * 初始化实例后马上被调用
     */
    fun bindVEContainer(veContainer: IContainer)

    /**
     * service 被销毁的时候调用
     */
    fun onStop()

}
