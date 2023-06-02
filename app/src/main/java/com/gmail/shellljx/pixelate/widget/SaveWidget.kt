package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class SaveWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs), IWidget {
    override fun bindVEContainer(veContainer: IContainer) {
    }

    override fun onWidgetActive() {
    }

    override fun onWidgetInactive() {
    }
}