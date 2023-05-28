package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import com.gmail.shellljx.pixelate.extension.dp
import kotlin.math.*

class CircleSeekbarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var listener: OnSeekPercentListener? = null
    private val maxRadius = 10f.dp().toFloat()
    private val minRadius = 6f.dp().toFloat()
    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mTouchSlop: Int
    private val rect = RectF()
    private var lastX = 0f
    private var inMove = false
    private var leftLimit = 0f
    private var rightLimit = 0f
    private var percent = 0f

    init {
        circlePaint.color = Color.WHITE
        circlePaint.style = Paint.Style.FILL
        circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)

        // 设置白色边框
        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.FILL
        borderPaint.alpha = 150
        borderPaint.strokeWidth = 1.dp().toFloat()
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val x = 10
        val y = (h - 20) / 2f
        rect.set(x.toFloat(), y, w - 10f, y + 20)
        leftLimit = 10 + minRadius
        rightLimit = width - maxRadius - 10
        lastX = leftLimit
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制白色边框
        canvas.drawRoundRect(rect, 10f, 10f, borderPaint)

        val centerX = lastX
        val radius = minRadius + (maxRadius - minRadius) * percent
        canvas.drawCircle(centerX, rect.centerY(), radius, circlePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                inMove = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                var x = event.x
                if (abs(x - lastX) < mTouchSlop && !inMove) {
                    return false
                }
                if (!inMove) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    listener?.onSeekStart()
                }
                if (x < leftLimit) {
                    x = leftLimit
                } else if (x > rightLimit) {
                    x = rightLimit
                }
                inMove = true
                var invalidate = false
                if (lastX != x) {
                    lastX = x
                    percent = (lastX - leftLimit) / (rightLimit - leftLimit)
                    listener?.onSeekPercent(percent)
                    invalidate = true
                }
                if (invalidate) {
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                listener?.onSeekComplete()
            }
        }
        return false
    }

    fun setSeekPercentListener(listener: OnSeekPercentListener) {
        this.listener = listener
    }

    interface OnSeekPercentListener {
        fun onSeekStart()
        fun onSeekPercent(percent: Float)
        fun onSeekComplete()
    }
}