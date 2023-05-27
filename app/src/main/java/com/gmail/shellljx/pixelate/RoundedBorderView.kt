package com.gmail.shellljx.pixelate

import android.view.View

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import com.gmail.shellljx.pixelate.extension.dp

class RoundedBorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val borderPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPath: Path = Path()
    private val borderRect: RectF = RectF()

    private var cornerRadius: Float = 5.dp().toFloat()
    private var borderWidth: Float = 4.dp().toFloat()
    private var borderColor: Int = Color.WHITE

    init {
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = borderWidth
        borderPaint.color = borderColor

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // 绘制边框
        if (borderWidth > 0) {
            val halfBorderWidth = borderWidth / 2f
            borderRect.set(halfBorderWidth, halfBorderWidth, width - halfBorderWidth, height - halfBorderWidth)
            borderPath.reset()
            borderPath.addRoundRect(borderRect, cornerRadius, cornerRadius, Path.Direction.CW)
            canvas.drawPath(borderPath, borderPaint)
        }
    }
}
