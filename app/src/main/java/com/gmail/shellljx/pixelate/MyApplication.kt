package com.gmail.shellljx.pixelate

import androidx.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.gmail.shellljx.pixelate.utils.DensityUtils

class MyApplication : MultiDexApplication() {
    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        DensityUtils.init(this)
        Fresco.initialize(this)
    }
}