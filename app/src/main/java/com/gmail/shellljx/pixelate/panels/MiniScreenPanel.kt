package com.gmail.shellljx.pixelate.panels

import android.app.Activity
import android.graphics.*
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.gmail.shellljx.pixelate.*
import com.gmail.shellljx.pixelator.IPixelator
import kotlin.math.round

class MiniScreenPanel(private val context: Activity) : IPanel, SurfaceHolder.Callback {
    private var miniScreen: SurfaceView? = null
    var imageSdk: IPixelator? = null

    override fun onCreateView(parent: ViewGroup) {
        if (miniScreen == null) {
            miniScreen = SurfaceView(context)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            lp.setMargins(4, 4, 4, 4)
            parent.addView(miniScreen, lp)
            val roundView = RoundedBorderView(context)
            val lp2 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            parent.addView(roundView, lp2)

            val circleView = CircleView(context)
            val lp3= FrameLayout.LayoutParams(200,200)
            lp3.gravity = Gravity.CENTER
            parent.addView(circleView, lp3)
        }
        onViewCreated()
    }

    override fun onViewCreated() {
        miniScreen?.holder?.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        imageSdk?.getMiniScreen()?.onSurfaceCreated(holder.surface)
        imageSdk?.refreshFrame()
    }

    override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
        imageSdk?.getMiniScreen()?.onSurfaceChanged(width, height)
        imageSdk?.refreshFrame()
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        imageSdk?.getMiniScreen()?.onSurfaceDestroy()
    }
}