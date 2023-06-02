package com.gmail.shellljx.wrapper.widget

import com.gmail.shellljx.wrapper.IContainer

interface IWidget {
    /**
     * 绑定 vecontainer
     */
    fun bindVEContainer(container: IContainer)

    /**
     * 可能需要widget和widget之间直接通信更方便，后续看有没有更好的办法，有点违背了widget之间互相隔离的原则
     * @param key 消息key
     * @param args 消息数据
     */
    fun onWidgetMessage(key: String, vararg args: Any) {}

    /**
     * 控件被添加到视图树激活
     */
    fun onWidgetActive()

    /**
     * 控件被移除视图树
     */
    fun onWidgetInactive()
}
