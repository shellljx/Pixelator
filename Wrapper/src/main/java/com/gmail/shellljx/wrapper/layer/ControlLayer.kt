package com.gmail.shellljx.wrapper.layer

import android.view.View
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.control.IControlContainer

class ControlLayer(private val veContainer: IContainer, index: Int) : AbsBuildInLayer(index) {
    private var controlContainer: IControlContainer? = null
    override fun getView(): View? {
        if (controlContainer == null) {
            controlContainer = veContainer.getControlService()?.createView(veContainer.getContext())
            veContainer.getConfig().controlContainerConfig?.let {
                controlContainer?.setConfig(it)
            }
        }
        return controlContainer?.getView()
    }

    override fun toString(): String {
        return "[BuildInLayer] ControlLayer index ${getIndex()}"
    }
}
