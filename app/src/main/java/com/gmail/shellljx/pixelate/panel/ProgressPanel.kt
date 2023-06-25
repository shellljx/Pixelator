package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.view.View
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/24
 * @Description:
 */
class ProgressPanel(context: Context) : AbsPanel(context) {

    override val tag: String
        get() = "ProgressPanel"

    override fun onBindVEContainer(container: IContainer) {
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_progress_layout
    }

    override fun onViewCreated(view: View?) {
    }

}