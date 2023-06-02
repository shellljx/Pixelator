package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class MiniScreenWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), IWidget {
    override fun bindVEContainer(veContainer: IContainer) {

    }

    override fun onWidgetActive() {
    }

    override fun onWidgetInactive() {
    }
}