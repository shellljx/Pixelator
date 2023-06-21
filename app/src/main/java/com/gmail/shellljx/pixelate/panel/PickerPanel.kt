package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.view.View
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/21
 * @Description:
 */
class PickerPanel(context: Context) : AbsPanel(context) {

    override val tag: String
        get() = "PickerPanel"

    private lateinit var mContainer:IContainer

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun getLayoutId(): Int {
        return -1
    }

    override fun onViewCreated(view: View?) {
    }
}