package com.gmail.shellljx.pixelator

import androidx.annotation.IntDef
import com.gmail.shellljx.pixelator.MaskMode.Companion.BACKGROUND
import com.gmail.shellljx.pixelator.MaskMode.Companion.NONE
import com.gmail.shellljx.pixelator.MaskMode.Companion.PERSON

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/8
 * @Description:
 */

@IntDef(
    NONE, PERSON, BACKGROUND
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class MaskMode {
    companion object {
        const val NONE = 0
        const val PERSON = 1
        const val BACKGROUND = 2
    }
}
