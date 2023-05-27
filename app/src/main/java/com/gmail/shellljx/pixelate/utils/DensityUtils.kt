package com.gmail.shellljx.pixelate.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Display
import android.view.WindowManager

@SuppressLint("StaticFieldLeak") object DensityUtils {
    private lateinit var context: Context
    var screenW = 0
    var screenH = 0

    private var hasInit = false
    private var density //屏幕密度，dip/160
            = 0f
    private val display: Display? = null
    private var densityDpi //dpi，像素密度
            = 0
    private var displayMetrics: DisplayMetrics? = null

    fun init(context: Context) {
        this.context = context
        val dm = DisplayMetrics()
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.defaultDisplay.getMetrics(dm)

        if (dm.heightPixels > dm.widthPixels) {
            screenW = dm.widthPixels
            screenH = dm.heightPixels
        } else {
            screenW = dm.heightPixels
            screenH = dm.widthPixels
        }
        density = context.resources.displayMetrics.density
        densityDpi = dm.densityDpi
        displayMetrics = context.resources.displayMetrics
        hasInit = true
    }

    fun dip2px(dipValue: Float): Int {
        if (!this::context.isInitialized) {
            return 0
        }
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dipValue, displayMetrics
        ).toInt()
    }
}