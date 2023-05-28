package com.gmail.shellljx.wrapper.layer

import android.view.View
import com.gmail.shellljx.wrapper.IContainer

class RenderLayer(private val veContainer: IContainer, index: Int) : AbsBuildInLayer(index) {
    private var mRenderContainer: View? = null
    override fun getView(): View? {
        if (mRenderContainer == null) {
            mRenderContainer = veContainer.getRenderService()?.createView(veContainer.getContext())
        }
        return mRenderContainer
    }

    override fun toString(): String {
        return "[BuildInLayer] RenderLayer index ${getIndex()}"
    }
}
