package com.gmail.shellljx.wrapper.service.panel

import android.view.View
import android.view.ViewGroup
import com.gmail.shellljx.wrapper.IContainer

interface IPanel {
    val tag: String

    /**
     * 绑定 Container 在 panel 实例化之后就会注入
     */
    fun bindVEContainer(container: IContainer)

    /**
     * 更新数据
     */
    fun updatePayload(any: Any)

    /**
     * 返回view
     */
    fun getView(): View?

    /**
     * 创建view
     */
    fun createView(container: ViewGroup): View?

    /**
     * 添加到视图树上
     */
    fun attach()

    /**
     * 处在页面栈的顶层
     */
    fun resume()

    /**
     * 被其他panel覆盖
     */
    fun pause()

    /**
     * 从视图树上移除
     */
    fun detach()

    /**
     * 是否已经添加到视图树上
     */
    fun isAttached(): Boolean

    /**
     * 是否处在页面栈的顶层
     */
    fun isShowing(): Boolean
}
