package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.FrameLayout
import com.gmail.shellljx.pixelate.extension.dp


/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/23
 * @Description:
 */
class ClipRoundView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    override fun dispatchDraw(canvas: Canvas) {
        val path = Path()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val cornerRadius = 10.dp().toFloat() // 设置圆角半径的大小，根据需要进行调整
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
    }

}