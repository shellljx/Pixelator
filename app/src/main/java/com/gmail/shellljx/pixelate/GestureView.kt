package com.gmail.shellljx.pixelate

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class GestureView : View {
    private var listener: GestureListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, def: Int) : super(context, attributeSet, def)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                listener?.onMove(event.x, event.y)
            }
        }
        return super.onTouchEvent(event)
    }

    fun setGestureListener(listener: GestureListener) {
        this.listener = listener
    }

    interface GestureListener {
        fun onMove(x: Float, y: Float)
    }
}
