package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.service.IPixelatorCoreService
import com.gmail.shellljx.pixelate.service.PixelatorCoreService
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class CompareWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageView(context, attrs), IWidget {

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mCoreService?.setCanvasHide(true)
            }

            MotionEvent.ACTION_UP -> {
                mCoreService?.setCanvasHide(false)
            }
        }
        return true
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