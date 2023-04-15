package com.gmail.shellljx.pixelate

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration

class GestureView : View {
    private var listener: GestureListener? = null
    private var mTouchSlop: Int
    private var mLastPoint = PointF()
    private var mCurrentPoint = PointF()
    private var mFromPoint = PointF()
    private var mToPoint = PointF()
    private var mControlPoint = PointF()
    private var mInMove = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, def: Int) : super(context, attributeSet, def) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.x
                val y = event.y
                mLastPoint.set(x, y)
                mCurrentPoint.set(x, y)
                mFromPoint.set(x, y)
                mToPoint.set(x, y)
                mControlPoint.set(x, y)
                mInMove = false
            }
            MotionEvent.ACTION_MOVE -> {
                mCurrentPoint.set(event.x, event.y)
                if (PointUtils.distanceTo(mFromPoint, mCurrentPoint) < mTouchSlop && !mInMove) {
                    return true
                }
                mInMove = true
                if (mFromPoint.equals(mCurrentPoint.x, mCurrentPoint.y)) {
                    return true
                }
                mToPoint.set((mLastPoint.x + mCurrentPoint.x) / 2f, (mLastPoint.y + mCurrentPoint.y) / 2f)
                mControlPoint.set(mLastPoint)
                val list = PointUtils.pointsWith(mFromPoint, mToPoint, mControlPoint, 15f)
                listener?.onMove(list)
                mFromPoint.set(mToPoint)
                mLastPoint.set(mCurrentPoint)
            }
        }
        return super.onTouchEvent(event)
    }

    fun setGestureListener(listener: GestureListener) {
        this.listener = listener
    }

    interface GestureListener {
        fun onMove(points: List<PointF>)
    }
}
