package com.gmail.shellljx.pixelate.panels

import android.app.Activity
import android.graphics.*
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import com.gmail.shellljx.pixelate.*
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelator.IPixelator
import kotlin.math.round

class MiniScreenPanel(private val context: Activity) : IPanel, SurfaceHolder.Callback {
    private var miniScreen: SurfaceView? = null
    private var circleView: CircleView? = null
    var imageSdk: IPixelator? = null
    private var parent: ViewGroup? = null

    override fun onCreateView(parent: ViewGroup) {
        this.parent = parent
        if (miniScreen == null) {
            miniScreen = SurfaceView(context)
            val lp = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            lp.setMargins(2.dp(), 2.dp(), 2.dp(), 2.dp())
            parent.addView(miniScreen, lp)
            val roundView = RoundedBorderView(context)
            val lp2 = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            parent.addView(roundView, lp2)

            circleView = CircleView(context)
            val lp3 = FrameLayout.LayoutParams(100, 100)
            lp3.gravity = Gravity.CENTER
            parent.addView(circleView, lp3)
        }
        onViewCreated()
    }

    fun translate(x: Float, y: Float, bounds: RectF) {
        val spaceX = (miniScreen?.width ?: return) / 2f
        val spaceY = (miniScreen?.height ?: return) / 2f
        val circleRadius = (circleView?.width ?: return) / 2f
        if (bounds.bottom - y < 0) {
            parent?.visibility = View.GONE
            miniScreen?.visibility = View.GONE
            return
        } else if (parent?.visibility == View.GONE) {
            parent?.visibility = View.VISIBLE
            miniScreen?.visibility = View.VISIBLE
        }
        if (x - bounds.left < spaceX) {
            if (x - bounds.left < (circleRadius + 2.dp())) {
                circleView?.translationX = circleRadius - (spaceX - 2.dp())
            } else {
                circleView?.translationX = (x - bounds.left) - spaceX
            }
        } else if (bounds.right - x < spaceX) {
            if (bounds.right - x < (circleRadius + 2.dp())) {
                circleView?.translationX = (spaceX - 2.dp()) - circleRadius
            } else {
                circleView?.translationX = spaceX - (bounds.right - x)
            }
        } else {
            circleView?.translationX = 0f
        }
        if (y - bounds.top < spaceY) {
            if (y - bounds.top < (circleRadius + 2.dp())) {
                circleView?.translationY = circleRadius - (spaceY - 2.dp())
            } else {
                circleView?.translationY = (y - bounds.top) - spaceY
            }
        } else if (bounds.bottom - y < spaceY) {
            if (bounds.bottom - y < (circleRadius + 2.dp())) {
                circleView?.translationY = (spaceY - 2.dp()) - circleRadius
            } else {
                circleView?.translationY = spaceY - (bounds.bottom - y)
            }
        } else {
            circleView?.translationY = 0f
        }
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