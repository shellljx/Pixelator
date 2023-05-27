package com.gmail.shellljx.pixelate

import android.app.Application
import com.gmail.shellljx.pixelate.utils.DensityUtils

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DensityUtils.init(this)
    }
}