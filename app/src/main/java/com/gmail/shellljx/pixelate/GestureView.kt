package com.gmail.shellljx.pixelate

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

    var editEnable: Boolean = true
    private var mActivePointerId1 = INVALID_POINTER_ID
    private var mActivePointerId2 = INVALID_POINTER_ID
    var transformMatrix = Matrix()

    private var listener: GestureListener? = null
    private var transformProcessor = TransformProcessor()
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
    private val bounds = RectF()
    private val paint = Paint()
    private var textureWidth = 0
    private var textureHeight = 0
    private var x = 0
    private var y = 0

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
                        val list = PointUtils.pointsWith(mFromPoint, mToPoint, mControlPoint, 50f)
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
                        //listener?.onTranslate(mLastScale, 0f, 0f, mRotation, mTranslateX, mTranslateY)
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
        return true
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
        transformMatrix.postScale(scale, scale, (x1 + x2) / 2f, (y1 + y2) / 2f)
        val dx = (x1 + x2) / 2f - (mLastPoint.x + mLastPoint2.x) / 2f
        val dy = (y1 + y2) / 2f - (mLastPoint.y + mLastPoint2.y) / 2f
        transformMatrix.postTranslate(dx, dy)

        bounds.set(x.toFloat(), y.toFloat(), x + textureWidth.toFloat(), y + textureHeight.toFloat())
        transformMatrix.mapRect(bounds)
        listener?.refresh(transformMatrix)

        mLastAngle = angle.toFloat()
        mLastScale *= scale
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

    fun initFrame(x: Int, y: Int, width: Int, height: Int) {
        //transformMatrix.setTranslate(x.toFloat(), y.toFloat())
        textureWidth = width
        textureHeight = height
        this.x = x
        this.y = y
    }

    interface GestureListener {
        fun onMove(points: List<PointF>)
        fun onTranslate(scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float)
        fun refresh(matrix: Matrix)
    }
}
