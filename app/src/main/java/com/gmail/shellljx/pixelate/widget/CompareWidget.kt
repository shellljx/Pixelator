package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class CompareWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageView(context, attrs), IWidget {

    private lateinit var mContainer: IContainer
    override fun bindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun onWidgetActive() {
        mContainer.getControlService()?.registerWidgetMessage(this, WidgetEvents.MSG_TRANSLATE_PROGRESS)
    }

    override fun onWidgetMessage(key: String, vararg args: Any) {
        if (key == WidgetEvents.MSG_TRANSLATE_PROGRESS) {
            val progress = (args[0] as? Float) ?: 0f
            translationX = progress * 100.dp()
            alpha = 1 - progress
            requestLayout()
        }
    }

    override fun onWidgetInactive() {
        mContainer.getControlService()?.unregisterWidgetMessage(this)
    }
}