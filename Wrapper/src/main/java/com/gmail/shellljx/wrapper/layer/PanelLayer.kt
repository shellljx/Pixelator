package com.gmail.shellljx.wrapper.layer

import android.view.View
import com.gmail.shellljx.wrapper.IContainer

class PanelLayer(private val veContainer: IContainer, index: Int) : AbsBuildInLayer(index) {
    private var mPanelContainer: View? = null
    override fun getView(): View? {
        if (mPanelContainer == null) {
            mPanelContainer = veContainer.getPanelService()?.createView(veContainer.getContext())
        }
        return mPanelContainer
    }

    override fun toString(): String {
        return "[BuildInLayer] PanelLayer index ${getIndex()}"
    }
}
