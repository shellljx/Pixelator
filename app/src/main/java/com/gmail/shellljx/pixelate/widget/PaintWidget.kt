package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import com.gmail.shellljx.pixelate.view.CircleView
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class PaintWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CircleView(context, attrs), IWidget {
    override fun bindVEContainer(veContainer: IContainer) {
    }

    override fun onWidgetActive() {
    }

    override fun onWidgetInactive() {
    }
}