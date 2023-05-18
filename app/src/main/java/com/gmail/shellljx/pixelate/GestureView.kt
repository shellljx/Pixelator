package com.gmail.shellljx.pixelate

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.atan2
import kotlin.math.sqrt

class GestureView : View {

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    var editEnable: Boolean = true
    private var mActivePointerId1 = INVALID_POINTER_ID
    private var mActivePointerId2 = INVALID_POINTER_ID

    private var listener: GestureListener? = null
    private var mTouchSlop: Int
    private var mLastPoint = PointF()
    private var mLastPoint2 = PointF()
    private var mCurrentPoint = PointF()
    private var mFromPoint = PointF()
    private var mToPoint = PointF()
    private var mControlPoint = PointF()
    private var mLastAngle = 0f
    private var mRotation = 0f
    private var mLastScale = 1f
    private var mTranslateX = 0f
    private var mTranslateY = 0f
    private var mInMove = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, def: Int) : super(context, attributeSet, def) {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

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
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                mActivePointerId2 = event.getPointerId(event.actionIndex)
                val x = event.getX(event.findPointerIndex(mActivePointerId2))
                val y = event.getY(event.findPointerIndex(mActivePointerId2))
                mLastPoint2.set(x, y)
                mLastAngle = atan2((mLastPoint2.y - mLastPoint.y), (mLastPoint2.x - mLastPoint.x))
            }

            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId1 != INVALID_POINTER_ID && mActivePointerId2 != INVALID_POINTER_ID) {
                    scaleAndRotateByFlingers(event)
                } else if (mActivePointerId1 != INVALID_POINTER_ID) {
                    if (editEnable) {
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
                    } else {
                        val x = event.getX(event.findPointerIndex(mActivePointerId1))
                        val y = event.getY(event.findPointerIndex(mActivePointerId1))
                        val dx = x - mLastPoint.x
                        val dy = mLastPoint.y - y
                        mTranslateX += dx
                        mTranslateY += dy
                        listener?.onTranslate(mLastScale,0f,0f, mRotation, mTranslateX, mTranslateY)
                        mLastPoint.set(x, y)
                    }
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mActivePointerId2 = INVALID_POINTER_ID
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId1 = INVALID_POINTER_ID
                mActivePointerId2 = INVALID_POINTER_ID
            }
        }
        return super.onTouchEvent(event)
    }

    private fun scaleAndRotateByFlingers(event: MotionEvent) {
        val x1 = event.getX(event.findPointerIndex(mActivePointerId1))
        val y1 = event.getY(event.findPointerIndex(mActivePointerId1))
        val x2 = event.getX(event.findPointerIndex(mActivePointerId2))
        val y2 = event.getY(event.findPointerIndex(mActivePointerId2))
        val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
        var scale = sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat() / sqrt(
            ((mLastPoint2.x - mLastPoint.x) * (mLastPoint2.x - mLastPoint.x) + (mLastPoint2.y - mLastPoint.y) * (mLastPoint2.y - mLastPoint.y)).toDouble()
        ).toFloat()

        mRotation += Math.toDegrees(mLastAngle - angle).toFloat()
        listener?.onTranslate(mLastScale * scale, (x1 + x2) / 2f, height - (y1 + y2) / 2f, mRotation, mTranslateX, mTranslateY)

        mLastAngle = angle.toFloat()
        mLastScale *= scale
        mLastPoint.set(x1, y1)
        mLastPoint2.set(x2, y2)
    }

    fun setGestureListener(listener: GestureListener) {
        this.listener = listener
    }

    interface GestureListener {
        fun onMove(points: List<PointF>)
        fun onTranslate(scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float)
    }
}
