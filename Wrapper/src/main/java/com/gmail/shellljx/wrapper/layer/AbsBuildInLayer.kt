package com.gmail.shellljx.wrapper.layer

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import kotlin.math.max

abstract class AbsBuildInLayer(private var index: Int) {
    fun increaseIndex() {
        index++
    }

    fun decreaseIndex() {
        index--
    }

    fun getIndex(): Int {
        return index
    }

    fun attach(parent: ViewGroup) {
        parent.addView(
            getView(), index,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
    }

    fun onWindowInsetsChanged(insets: Rect) {
        getView()?.setPadding(max(0, insets.left), max(0, insets.top), max(0, insets.right), max(0, insets.bottom))
    }

    abstract fun getView(): View?
}
