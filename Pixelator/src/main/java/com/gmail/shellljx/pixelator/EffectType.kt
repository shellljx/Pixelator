package com.gmail.shellljx.pixelator

import androidx.annotation.IntDef
import com.gmail.shellljx.pixelator.EffectType.Companion.TypeImage
import com.gmail.shellljx.pixelator.EffectType.Companion.TypeMosaic

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/12
 * @Description:
 */
@IntDef(
    TypeMosaic, TypeImage
)
@Target(AnnotationTarget.FIELD, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class EffectType {
    companion object {
        const val TypeMosaic = 0
        const val TypeImage = 1
    }
}