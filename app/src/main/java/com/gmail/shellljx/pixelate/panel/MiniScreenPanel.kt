package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.graphics.PointF
import android.view.*
import androidx.annotation.Keep
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.pixelate.view.CircleView
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.gesture.OnSingleMoveObserver
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
@Keep
class MiniScreenPanel(context: Context) : AbsPanel(context), SurfaceHolder.Callback, OnSingleMoveObserver, PaintSizeObserver {
    override val tag: String
        get() = "MiniScreenPanel"

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var miniScreen: SurfaceView? = null
    private var pointView: CircleView? = null

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = container.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_miniscreen_layout
    }

    override fun onViewCreated(view: View?) {
        miniScreen = view?.findViewById(R.id.mini_surfaceview)
        pointView = view?.findViewById(R.id.point_view)
        miniScreen?.holder?.addCallback(this)
        mContainer.getGestureService()?.addSingleMoveObserver(this)
        mCoreService?.addPaintSizeObserver(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mCoreService?.getMiniScreen()?.onSurfaceCreated(holder.surface)
    }

    override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
        mCoreService?.getMiniScreen()?.onSurfaceChanged(width, height)
    }

    override fun surfaceDestroyed(p0: SurfaceHolder) {
        mCoreService?.getMiniScreen()?.onSurfaceDestroy()
    }

    override fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF): Boolean {
        updatePointViewLocation(current.x, current.y)
        return false
    }

    private fun updatePointViewLocation(x: Float, y: Float) {
        val bounds = mCoreService?.getContentBounds() ?: return
        val spaceX = (miniScreen?.width ?: return) / 2f
        val spaceY = (miniScreen?.height ?: return) / 2f
        val circleRadius = (pointView?.width ?: return) / 2f
        if (bounds.bottom - y < 0) {
            getView()?.visibility = View.GONE
            miniScreen?.visibility = View.GONE
            return
        } else if (getView()?.visibility == View.GONE) {
            getView()?.visibility = View.VISIBLE
            miniScreen?.visibility = View.VISIBLE
        }
        if (x - bounds.left < spaceX) {
            if (x - bounds.left < (circleRadius + 2.dp())) {
                pointView?.translationX = circleRadius - (spaceX - 2.dp())
            } else {
                pointView?.translationX = (x - bounds.left) - spaceX
            }
        } else if (bounds.right - x < spaceX) {
            if (bounds.right - x < (circleRadius + 2.dp())) {
                pointView?.translationX = (spaceX - 2.dp()) - circleRadius
            } else {
                pointView?.translationX = spaceX - (bounds.right - x)
            }
        } else {
            pointView?.translationX = 0f
        }
        if (y - bounds.top < spaceY) {
            if (y - bounds.top < (circleRadius + 2.dp())) {
                pointView?.translationY = circleRadius - (spaceY - 2.dp())
            } else {
                pointView?.translationY = (y - bounds.top) - spaceY
            }
        } else if (bounds.bottom - y < spaceY) {
            if (bounds.bottom - y < (circleRadius + 2.dp())) {
                pointView?.translationY = (spaceY - 2.dp()) - circleRadius
            } else {
                pointView?.translationY = spaceY - (bounds.bottom - y)
            }
        } else {
            pointView?.translationY = 0f
        }
    }

    override fun onPaintSizeChanged(size: Int) {
        val lp = pointView?.layoutParams?.apply {
            height = size
            width = size
        }
        pointView?.layoutParams = lp
    }
}