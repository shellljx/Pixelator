package com.gmail.shellljx.pixelate.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.widget.ViewAnimator
import androidx.core.view.isVisible
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.pixelate.view.CircleView
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class PaintWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CircleView(context, attrs), IWidget, PaintSizeObserver {
    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var mHideAnimator: Animator? = null
    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onWidgetMessage(key: String, vararg args: Any) {
        when (key) {
            WidgetEvents.MSG_SHOW_FINGER_POINT -> {
                alpha = 1f
                isVisible = true
            }

            WidgetEvents.MSG_HIDE_FINGER_POINT -> {
                startHideAnimator()
            }
        }
    }

    override fun onWidgetActive() {
        mCoreService?.addPaintSizeObserver(this)
        mContainer.getControlService()?.registerWidgetMessage(
            this,
            WidgetEvents.MSG_SHOW_FINGER_POINT,
            WidgetEvents.MSG_HIDE_FINGER_POINT
        )
    }

    override fun onWidgetInactive() {
        mContainer.getControlService()?.unregisterWidgetMessage(this)
        mHideAnimator?.cancel()
    }

    private fun startHideAnimator() {
        mHideAnimator?.cancel()
        if (mHideAnimator == null) {
            mHideAnimator = ObjectAnimator.ofFloat(this, ViewAnimator.ALPHA, 1f, 0f)
            mHideAnimator?.duration = 250
        }
        mHideAnimator?.start()
    }

    override fun onPaintSizeChanged(size: Int) {
        layoutParams.width = size
        layoutParams.height = size
        requestLayout()
    }
}