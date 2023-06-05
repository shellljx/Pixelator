package com.gmail.shellljx.pixelator

import androidx.annotation.IntDef

const val ERASER = 0
const val PAINT = 1

@IntDef(
    ERASER, PAINT
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class PaintType