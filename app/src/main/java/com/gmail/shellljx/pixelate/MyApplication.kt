package com.gmail.shellljx.pixelate

import androidx.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.gmail.shellljx.pixelate.utils.DensityUtils

class MyApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        DensityUtils.init(this)
        Fresco.initialize(this)
    }
}