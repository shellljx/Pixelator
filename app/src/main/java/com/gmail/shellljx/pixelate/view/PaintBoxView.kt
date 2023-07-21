package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.gmail.shellljx.pixelate.extension.dp
import kotlin.math.max
import kotlin.math.min

/**
 * @Author: shell
 * @Email: shell@gmail.com
 * @Date: 2023/7/20
 * @Description:
 */
class PaintBoxView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val mStartPoint = PointF()
    private val mEndPoint = PointF()
    private val rectanglePaint: Paint = Paint()
    private val circlePaint: Paint
    private val mBoxRect = RectF()
    private var clear = false

    init {
        rectanglePaint.color = Color.WHITE
        rectanglePaint.style = Paint.Style.STROKE
        rectanglePaint.strokeWidth = 1.dp().toFloat()
        rectanglePaint.alpha = 200

        circlePaint = Paint()
        circlePaint.color = Color.WHITE
        circlePaint.style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        if (!clear) {
            mBoxRect.set(
                min(mStartPoint.x, mEndPoint.x),
                min(mStartPoint.y, mEndPoint.y),
                max(mStartPoint.x, mEndPoint.x),
                max(mStartPoint.y, mEndPoint.y)
            )
            canvas.drawRect(mBoxRect, rectanglePaint)
            canvas.drawCircle(mStartPoint.x, mStartPoint.y, 3.dp().toFloat(), circlePaint)
            canvas.drawCircle(mStartPoint.x, mEndPoint.y, 3.dp().toFloat(), circlePaint)
            canvas.drawCircle(mEndPoint.x, mStartPoint.y, 3.dp().toFloat(), circlePaint)
            canvas.drawCircle(mEndPoint.x, mEndPoint.y, 3.dp().toFloat(), circlePaint)
        }
    }

    fun setStartPoint(x: Float, y: Float) {
        mStartPoint.set(x, y)
    }

    fun setEndPoint(x: Float, y: Float) {
        clear = false
        mEndPoint.set(x, y)
        invalidate()
    }

    fun clear() {
        clear = true
        invalidate()
    }
}