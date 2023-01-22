package com.gmail.shellljx.pixelator

import android.view.Surface
import android.view.SurfaceHolder

class Pixelator private constructor() : IPixelator, SurfaceHolder.Callback {

    private var mId = 0L

    init {
        mId = create()
    }

    override fun surfaceCreated(p0: SurfaceHolder) {
        if (mId != 0L) {
            onSurfaceCreate(mId, p0.surface)
        }
    }

    override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
    }

    private fun onEGLContextCreate() {
        System.out.println("lijinxiang onEGLContextCreate")
    }

    private fun onEGLWindowCreate() {
        System.out.println("lijinxiang onEGLWindowCreate")
    }

    private external fun create(): Long
    private external fun onSurfaceCreate(id: Long, surface: Surface)

    companion object {
        // Used to load the 'pixelator' library on application startup.
        init {
            System.loadLibrary("pixelator")
        }

        fun create(): IPixelator {
            return Pixelator()
        }
    }
}
