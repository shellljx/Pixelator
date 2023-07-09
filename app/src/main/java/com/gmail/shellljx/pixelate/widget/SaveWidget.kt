package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.service.IPixelatorCoreService
import com.gmail.shellljx.pixelate.service.PixelatorCoreService
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class SaveWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs), IWidget, OnClickListener {
    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null

    init {
        setOnClickListener(this)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onWidgetActive() {
        mContainer.getControlService()?.registerWidgetMessage(this, WidgetEvents.MSG_TRANSLATE_PROGRESS)
    }

    override fun onWidgetMessage(key: String, vararg args: Any) {
        if (key == WidgetEvents.MSG_TRANSLATE_PROGRESS) {
            val progress = (args[0] as? Float) ?: 0f
            translationX = progress * 100.dp()
            alpha = 1- progress
            requestLayout()
        }
    }

    override fun onWidgetInactive() {
        mContainer.getControlService()?.unregisterWidgetMessage(this)
    }

    override fun onClick(v: View?) {
        mCoreService?.save()
    }
}