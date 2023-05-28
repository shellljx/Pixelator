package com.gmail.shellljx.wrapper.layer

import android.view.View
import com.gmail.shellljx.wrapper.IContainer

class GestureLayer(private val veContainer: IContainer, index: Int) : AbsBuildInLayer(index) {
    private var gestureView: View? = null
    override fun getView(): View? {
        if (gestureView == null) {
            gestureView = veContainer.getGestureService()?.createView(veContainer.getContext())
        }
        return gestureView
    }

    override fun toString(): String {
        return "[BuildInLayer] GestureLayer index ${getIndex()}"
    }
}
