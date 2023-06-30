package com.gmail.shellljx.wrapper.service.gesture

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import kotlin.math.pow
import kotlin.math.sqrt

class GestureContaienr : View {
    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    private var mActivePointerId1 = INVALID_POINTER_ID
    private var mActivePointerId2 = INVALID_POINTER_ID
    private var mListener: GestureListener? = null
    private var mTouchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var mLastPoint = PointF()
    private var mLastPoint2 = PointF()
    private var mCurrentPoint = PointF()
    private var mFromPoint = PointF()
    private var mToPoint = PointF()
    private var mControlPoint = PointF()
    private var mInMove = false
    private var mInTransform = false
    private var mGestureEnable = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId1 = event.getPointerId(0)
                val x = event.x
                val y = event.y
                mLastPoint.set(x, y)
                mCurrentPoint.set(x, y)
                mFromPoint.set(x, y)
                mToPoint.set(x, y)
                mControlPoint.set(x, y)
                mInMove = false
                mInTransform = false
                mListener?.onSingleDown(event)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mActivePointerId2 = event.getPointerId(event.actionIndex)
                val x = event.getX(event.findPointerIndex(mActivePointerId2))
                val y = event.getY(event.findPointerIndex(mActivePointerId2))
                mLastPoint2.set(x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!mGestureEnable) return false
                if (mActivePointerId1 != INVALID_POINTER_ID && mActivePointerId2 != INVALID_POINTER_ID) {
                    if (mActivePointerId1 == mActivePointerId2) return false
                    val x1 = event.getX(event.findPointerIndex(mActivePointerId1))
                    val y1 = event.getY(event.findPointerIndex(mActivePointerId1))
                    val point2Index = event.findPointerIndex(mActivePointerId2)
                    if (point2Index < 0) return false
                    val x2 = event.getX(point2Index)
                    val y2 = event.getY(point2Index)
                    if (!mInTransform) {
                        mListener?.onTransformStart(PointF(x1, y1), PointF(x2, y2))
                    }
                    mInMove = true
                    mInTransform = true
                    mListener?.onTransform(mLastPoint, mLastPoint2, PointF(x1, y1), PointF(x2, y2))
                    mLastPoint.set(x1, y1)
                    mLastPoint2.set(x2, y2)
                } else if (mActivePointerId1 != INVALID_POINTER_ID) {
                    mCurrentPoint.set(event.x, event.y)
                    if (distanceTo(mFromPoint, mCurrentPoint) < mTouchSlop && !mInMove) {
                        return true
                    }
                    mInMove = true
                    if (mFromPoint.equals(mCurrentPoint.x, mCurrentPoint.y)) {
                        return true
                    }
                    mToPoint.set((mLastPoint.x + mCurrentPoint.x) / 2f, (mLastPoint.y + mCurrentPoint.y) / 2f)
                    mControlPoint.set(mLastPoint)
                    mListener?.onSingleMove(mFromPoint, mToPoint, mControlPoint, mCurrentPoint)
                    mFromPoint.set(mToPoint)
                    mLastPoint.set(mCurrentPoint)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mActivePointerId2 = INVALID_POINTER_ID
            }

            MotionEvent.ACTION_UP -> {
                mActivePointerId1 = INVALID_POINTER_ID
                mActivePointerId2 = INVALID_POINTER_ID
                mListener?.onSingleUp(event)
                if (!mInMove) {
                    mListener?.onSingleTap()
                }
                if (mInTransform) {
                    mListener?.onTransformEnd()
                }
            }
        }
        return true
    }

    private fun distanceTo(from: PointF, to: PointF): Float {
        return sqrt((to.x - from.x).pow(2) + (to.y - from.y).pow(2))
    }

    fun setGestureListener(listener: GestureListener) {
        this.mListener = listener
    }

    fun setGestureEnable(enable: Boolean) {
        mGestureEnable = enable
    }

    interface GestureListener {
        fun onSingleDown(event: MotionEvent)
        fun onSingleUp(event: MotionEvent)
        fun onSingleTap()
        fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF)
        fun onTransformStart(point: PointF, point2: PointF)
        fun onTransform(lastPoint: PointF, lastPoint2: PointF, point: PointF, point2: PointF)
        fun onTransformEnd()
    }
}
