package com.gmail.shellljx.pixelate.service

import android.animation.*
import android.graphics.*
import android.view.MotionEvent
import com.gmail.shellljx.pixelate.RectfEvaluator
import com.gmail.shellljx.pixelate.panel.MiniScreenPanel
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.gesture.*
import com.gmail.shellljx.wrapper.service.panel.PanelToken
import kotlin.math.sqrt

class TransformService : ITransformService, OnSingleDownObserver, OnSingleUpObserver, OnTransformObserver {
    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var mMiniToken: PanelToken? = null
    private val innerBounds = RectF(0f, 50f, 1080f, 1500f)

    override fun onStart() {
        mContainer.getGestureService()?.addSingleDownObserver(this)
        mContainer.getGestureService()?.addSingleUpObserver(this)
        mContainer.getGestureService()?.addTransformObserver(this)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onStop() {
    }

    override fun onSingleDown(event: MotionEvent): Boolean {
        mContainer.getControlService()?.hide()
//        mMiniToken?.let { mContainer.getPanelService()?.showPanel(it) } ?: run {
//            mMiniToken = mContainer.getPanelService()?.showPanel(MiniScreenPanel::class.java)
//        }
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        mContainer.getControlService()?.show()
        //mMiniToken?.let { mContainer.getPanelService()?.hidePanel(it) }
        tryToKeepInInnerBounds()
        return false
    }

    private fun tryToKeepInInnerBounds() {
        val bounds = mCoreService?.getContentBounds() ?: return
        val transformMatrix = mCoreService?.getTransformMatrix()
        val from = RectF(bounds)
        val to = generateToRect() ?: return
        val animator = ObjectAnimator.ofObject(RectfEvaluator(), from, to)
        animator.duration = 200
        val lastRect: RectF = from
        animator.addUpdateListener {
            val rect = it.animatedValue as RectF
            val matrix = Matrix()
            matrix.setRectToRect(lastRect, rect, Matrix.ScaleToFit.CENTER)
            matrix.preConcat(transformMatrix)
            mCoreService?.setTransformMatrix(matrix)
            lastRect.set(rect)
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                mContainer.getGestureService()?.gestureEnable(false)
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                mContainer.getGestureService()?.gestureEnable(true)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                mContainer.getGestureService()?.gestureEnable(true)
            }
        })
        animator.start()
    }

    private fun generateToRect(): RectF? {
        val bounds = mCoreService?.getContentBounds() ?: return null
        val initBounds = mCoreService?.getInitBounds() ?: return null
        val to = RectF(bounds)
        if (to.width() / initBounds.width() < 0.5) {
            to.set(0f, 0f, initBounds.width() * 0.5f, initBounds.height() * 0.5f)
        }
        var offsetX = 0f
        var offsetY = 0f
        if (to.height() >= innerBounds.height()) {
            if (to.bottom < innerBounds.bottom) {
                offsetY = innerBounds.bottom - to.bottom
            } else if (to.top > innerBounds.top) {
                offsetY = innerBounds.top - to.top
            }
        } else {
            val newTop = (innerBounds.height() - to.height()) / 2f
            offsetY = newTop - to.top
        }

        if (to.width() >= innerBounds.width()) {
            if (to.right < innerBounds.right) {
                offsetX = innerBounds.right - to.right
            } else if (to.left > innerBounds.left) {
                offsetX = innerBounds.left - to.left
            }
        } else {
            val newLeft = (innerBounds.width() - to.width()) / 2f
            offsetX = newLeft - to.left
        }

        if (offsetX != 0f || offsetY != 0f) {
            to.offset(offsetX, offsetY)
            return to
        }
        return null
    }

    override fun onTransform(lastPoint: PointF, lastPoint2: PointF, point: PointF, point2: PointF): Boolean {
        val x1 = point.x
        val y1 = point.y
        val x2 = point2.x
        val y2 = point2.y

        val scale = sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble()).toFloat() / sqrt(
            ((lastPoint2.x - lastPoint.x) * (lastPoint2.x - lastPoint.x) + (lastPoint2.y - lastPoint.y) * (lastPoint2.y - lastPoint.y)).toDouble()
        ).toFloat()

        val transformMatrix = mCoreService?.getTransformMatrix() ?: return false
        val matrixValues = FloatArray(9)
        transformMatrix.getValues(matrixValues)
        val scaleX = matrixValues[Matrix.MSCALE_X]
        val scaleLimit = scaleX > 4f && scale > 1f
        if (!scaleLimit) {
            transformMatrix.postScale(scale, scale, (x1 + x2) / 2f, (y1 + y2) / 2f)
        }

        val dx = (x1 + x2) / 2f - (lastPoint.x + lastPoint2.x) / 2f
        val dy = (y1 + y2) / 2f - (lastPoint.y + lastPoint2.y) / 2f

        transformMatrix.postTranslate(dx, dy)
        mCoreService?.setTransformMatrix(transformMatrix)
        return true
    }
}

interface ITransformService : IService