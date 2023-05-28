package com.gmail.shellljx.pixelate

import android.animation.*
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.graphics.*
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

    private var mActivePointerId1 = INVALID_POINTER_ID
    private var mActivePointerId2 = INVALID_POINTER_ID
    private var transformMatrix = Matrix()

    private var listener: GestureListener? = null
    private var mTouchSlop: Int
    private var mLastPoint = PointF()
    private var mLastPoint2 = PointF()
    private var mCurrentPoint = PointF()
    private var mFromPoint = PointF()
    private var mToPoint = PointF()
    private var mControlPoint = PointF()
    private var mInMove = false
    private val bounds = RectF()
    private val paint = Paint()
    private val innerBounds = RectF(50f, 50f, 1000f, 1500f)

    init {
        paint.setColor(Color.BLUE)
        paint.strokeWidth = 10f
        paint.style = Paint.Style.STROKE
    }

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
            }

            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId1 != INVALID_POINTER_ID && mActivePointerId2 != INVALID_POINTER_ID) {
                    scaleAndRotateByFlingers(event)
                } else if (mActivePointerId1 != INVALID_POINTER_ID) {
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
                    val list = PointUtils.pointsWith(mFromPoint, mToPoint, mControlPoint, 50f)
                    listener?.onMove(list, mCurrentPoint)
                    mFromPoint.set(mToPoint)
                    mLastPoint.set(mCurrentPoint)
                }
            }

            MotionEvent.ACTION_POINTER_UP -> {
                mActivePointerId2 = INVALID_POINTER_ID
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId1 = INVALID_POINTER_ID
                mActivePointerId2 = INVALID_POINTER_ID
                animationRect()
            }
        }
        return true
    }

    private fun animationRect() {
        val from = RectF(bounds)
        val to = generateToRect()
        val animator = ObjectAnimator.ofObject(RectfEvaluator(), from, to)
        animator.duration = 200
        var lastRect: RectF = from
        animator.addUpdateListener {
            val rect = it.animatedValue as RectF
            val matrix = Matrix()
            matrix.setRectToRect(lastRect, rect, Matrix.ScaleToFit.CENTER)
            matrix.preConcat(transformMatrix)
            listener?.refresh(matrix)
            transformMatrix.set(matrix)
            lastRect.set(rect)
        }
        animator.start()
    }

    private fun generateToRect(): RectF {
        val to = RectF(bounds)
        if (bounds.height()>innerBounds.height()){
            if (bounds.bottom < innerBounds.bottom) {
                to.offset(0f, innerBounds.bottom - bounds.bottom)
            } else if (bounds.top > innerBounds.top) {
                to.offset(0f, innerBounds.top - bounds.top)
            }
        }else{
            to.offsetTo(bounds.left, (innerBounds.height() - bounds.height())/2f)
        }

        return to
    }

    private fun scaleAndRotateByFlingers(event: MotionEvent) {
        val x1 = event.getX(event.findPointerIndex(mActivePointerId1))
        val y1 = event.getY(event.findPointerIndex(mActivePointerId1))
        val x2 = event.getX(event.findPointerIndex(mActivePointerId2))
        val y2 = event.getY(event.findPointerIndex(mActivePointerId2))
        var scale = sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat() / sqrt(
            ((mLastPoint2.x - mLastPoint.x) * (mLastPoint2.x - mLastPoint.x) + (mLastPoint2.y - mLastPoint.y) * (mLastPoint2.y - mLastPoint.y)).toDouble()
        ).toFloat()


        transformMatrix.postScale(scale, scale, (x1 + x2) / 2f, (y1 + y2) / 2f)
        val dx = (x1 + x2) / 2f - (mLastPoint.x + mLastPoint2.x) / 2f
        val dy = (y1 + y2) / 2f - (mLastPoint.y + mLastPoint2.y) / 2f
        transformMatrix.postTranslate(dx, dy)

        listener?.refresh(transformMatrix)

        mLastPoint.set(x1, y1)
        mLastPoint2.set(x2, y2)
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawRect(bounds, paint)
    }

    fun setGestureListener(listener: GestureListener) {
        this.listener = listener
    }

    fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
        bounds.set(left, top, right, bottom)
        invalidate()
    }

    interface GestureListener {
        fun onMove(points: List<PointF>, current: PointF)
        fun refresh(matrix: Matrix)
    }
}
