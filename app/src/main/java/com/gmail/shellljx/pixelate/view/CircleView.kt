package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.gmail.shellljx.pixelate.extension.dp

open class CircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var radius: Float = 0f
    private var centerX: Float = 0f
    private var centerY: Float = 0f

    init {
        circlePaint.color = Color.parseColor("#80FFFFFF")
        circlePaint.style = Paint.Style.FILL
        circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)

        // 设置白色边框
        borderPaint.color = Color.WHITE
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = 1.dp().toFloat()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        radius = (Math.min(width, height) / 2).toFloat() - 1.dp().toFloat()
        centerX = (width / 2).toFloat()
        centerY = (height / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 清空画布
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        // 绘制半透明圆
        canvas.drawCircle(centerX, centerY, radius, circlePaint)

        // 绘制白色边框
        canvas.drawCircle(centerX, centerY, radius, borderPaint)
    }
}