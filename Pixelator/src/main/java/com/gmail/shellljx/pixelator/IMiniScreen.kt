package com.gmail.shellljx.pixelator

import android.view.Surface

interface IMiniScreen {
    fun onSurfaceCreated(surface: Surface)
    fun onSurfaceChanged(width: Int, height: Int)
    fun onSurfaceDestroy()
}