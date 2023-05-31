package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import com.gmail.shellljx.pixelate.PointUtils
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
    private val innerBounds = RectF(0f, 50f, 1080f, 1500f)
    private val initBounds = RectF()
    private var isAnimating = false

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
                if (isAnimating) {
                    return true
                }
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
                if (!isAnimating) {
                    animationRect()
                }
            }
        }
        return true
    }

    private fun animationRect() {
//        val from = RectF(bounds)
//        val to = generateToRect() ?: return
//        val animator = ObjectAnimator.ofObject(RectfEvaluator(), from, to)
//        animator.duration = 200
//        val lastRect: RectF = from
//        animator.addUpdateListener {
//            val rect = it.animatedValue as RectF
//            val matrix = Matrix()
//            matrix.setRectToRect(lastRect, rect, Matrix.ScaleToFit.CENTER)
//            matrix.preConcat(transformMatrix)
//            listener?.refresh(matrix)
//            transformMatrix.set(matrix)
//            lastRect.set(rect)
//        }
//        animator.addListener(object : AnimatorListenerAdapter() {
//            override fun onAnimationStart(animation: Animator?) {
//                isAnimating = true
//            }
//
//            override fun onAnimationEnd(animation: Animator?) {
//                isAnimating = false
//            }
//
//            override fun onAnimationCancel(animation: Animator?) {
//                isAnimating = false
//            }
//        })
//        animator.start()
    }

    private fun generateToRect(): RectF? {
        val to = RectF(bounds)
        if (to.width() / initBounds.width() < 0.5) {
            to.set(0f, 0f, initBounds.width() * 0.5f, initBounds.height() * 0.5f)
        }
        var offsetX = 0f
        var offsetY = 0f
        if (to.height() >= innerBounds.height()) {
            if (to.bottom < innerBounds.bottom) {
                offsetY = innerBounds.bottom - to.bottom
            } else if (to.top > innerBounds.top) {
                offsetY = innerBounds.top - to.top
            }
        } else {
            val newTop = (innerBounds.height() - to.height()) / 2f
            offsetY = newTop - to.top
        }

        if (to.width() >= innerBounds.width()) {
            if (to.right < innerBounds.right) {
                offsetX = innerBounds.right - to.right
            } else if (to.left > innerBounds.left) {
                offsetX = innerBounds.left - to.left
            }
        } else {
            val newLeft = (innerBounds.width() - to.width()) / 2f
            offsetX = newLeft - to.left
        }

        if (offsetX != 0f || offsetY != 0f) {
            to.offset(offsetX, offsetY)
            return to
        }
        return null
    }

    private fun scaleAndRotateByFlingers(event: MotionEvent) {
        if (mActivePointerId1 == mActivePointerId2) return
        val x1 = event.getX(event.findPointerIndex(mActivePointerId1))
        val y1 = event.getY(event.findPointerIndex(mActivePointerId1))
        val x2 = event.getX(event.findPointerIndex(mActivePointerId2))
        val y2 = event.getY(event.findPointerIndex(mActivePointerId2))
        val scale = sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat() / sqrt(
            ((mLastPoint2.x - mLastPoint.x) * (mLastPoint2.x - mLastPoint.x) + (mLastPoint2.y - mLastPoint.y) * (mLastPoint2.y - mLastPoint.y)).toDouble()
        ).toFloat()

        val matrixValues = FloatArray(9)
        transformMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleLimit = scaleX > 4f && scale > 1f
        if (!scaleLimit) {
            transformMatrix.postScale(scale, scale, (x1 + x2) / 2f, (y1 + y2) / 2f)
        }

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
        if (initBounds.isEmpty) {
            initBounds.set(bounds)
        }
        invalidate()
    }

    interface GestureListener {
        fun onMove(points: List<PointF>, current: PointF)
        fun refresh(matrix: Matrix)
    }
}
