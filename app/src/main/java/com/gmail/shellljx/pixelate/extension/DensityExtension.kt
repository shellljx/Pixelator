package com.gmail.shellljx.pixelate.extension

import com.gmail.shellljx.pixelate.utils.DensityUtils

inline fun Int.dp(): Int {
    return DensityUtils.dip2px(this.toFloat())
}

inline fun Float.dp(): Int {
    return DensityUtils.dip2px(this)
}