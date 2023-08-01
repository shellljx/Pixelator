package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/7/31
 * @Description:
 */
class SlideViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.BLACK)
    }

    private var slideChildView: View? = null


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)

    }

}