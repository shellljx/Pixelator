package com.gmail.shellljx.wrapper.layer

import android.graphics.Rect
import android.view.View

interface ILayer {
    //会被调用多次
    fun view(): View
    fun alignType(): AlignType
    fun onViewportUpdate(offset: Int) {}
    fun onWindowInsetsChanged(insets: Rect) {}
}

enum class AlignType {
    ALIGN_LAYER_CONTAINER,
    ALIGN_RENDER_LAYER
}
