package com.gmail.shellljx.pixelate

import android.animation.TypeEvaluator
import android.graphics.RectF

class RectfEvaluator : TypeEvaluator<RectF> {
    override fun evaluate(fraction: Float, startValue: RectF, endValue: RectF): RectF {
        val left = startValue.left + ((endValue.left - startValue.left) * fraction)
        val top = startValue.top + ((endValue.top - startValue.top) * fraction)
        val right = startValue.right + ((endValue.right - startValue.right) * fraction)
        val bottom = startValue.bottom + ((endValue.bottom - startValue.bottom) * fraction)
        return RectF(left, top, right, bottom)
    }
}