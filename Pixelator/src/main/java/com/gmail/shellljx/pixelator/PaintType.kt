package com.gmail.shellljx.pixelator

import androidx.annotation.IntDef

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/7/4
 * @Description:
 */

const val Graffiti = 0
const val Rect = 1

@IntDef(
    Graffiti, Rect
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class PaintType