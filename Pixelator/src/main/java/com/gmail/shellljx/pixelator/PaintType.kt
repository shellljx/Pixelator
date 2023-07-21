package com.gmail.shellljx.pixelator

import androidx.annotation.IntDef
import com.gmail.shellljx.pixelator.PaintType.Companion.Circle
import com.gmail.shellljx.pixelator.PaintType.Companion.Graffiti
import com.gmail.shellljx.pixelator.PaintType.Companion.Rect

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/7/4
 * @Description:
 */

@IntDef(
    Graffiti, Rect, Circle
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class PaintType {
    companion object {
        const val Graffiti = 0
        const val Rect = 1
        const val Circle = 2
    }
}