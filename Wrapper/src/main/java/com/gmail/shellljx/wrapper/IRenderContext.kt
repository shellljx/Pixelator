package com.gmail.shellljx.wrapper

import android.view.Surface

interface IRenderContext {
    fun setDisplaySuerface(surface: Surface)
    fun updateSurfaceChanged(width: Int, height: Int)
    fun destroyDisplaySurface()
}