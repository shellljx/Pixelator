package com.gmail.shellljx.pixelate.service

import android.animation.*
import android.graphics.*
import android.view.MotionEvent
import androidx.annotation.Keep
import androidx.core.graphics.contains
import com.gmail.shellljx.pixelate.RectfEvaluator
import com.gmail.shellljx.pixelate.panel.MiniScreenPanel
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.AbsService
import com.gmail.shellljx.wrapper.service.gesture.*
import com.gmail.shellljx.wrapper.service.panel.PanelToken
import kotlin.math.sqrt

@Keep
class TransformService(container: IContainer) : AbsService(container), ITransformService, OnTapObserver, OnTransformObserver, OnSingleMoveObserver {
    private var mCoreService: IPixelatorCoreService? = null
    private var mMiniToken: PanelToken? = null
    private val innerBounds = RectF()
    private var scaleAndTranslate = true

    override fun onStart() {
        container.getGestureService()?.addTapObserver(this)
        container.getGestureService()?.addTransformObserver(this)
        container.getGestureService()?.addSingleMoveObserver(this)
    }

    override fun bindVEContainer(container: IContainer) {
        mCoreService = container.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onStop() {
    }

    override fun onStartSingleMove(): Boolean {
        container.getControlService()?.hide()
        mMiniToken?.let { container.getPanelService()?.showPanel(it) } ?: run {
            mMiniToken = container.getPanelService()?.showPanel(MiniScreenPanel::class.java)
        }
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        container.getControlService()?.show()
        mMiniToken?.let { container.getPanelService()?.hidePanel(it) }
        return false
    }

    override fun tryKeepInInnerBounds() {
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
                container.getGestureService()?.gestureEnable(false)
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                container.getGestureService()?.gestureEnable(true)
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                container.getGestureService()?.gestureEnable(true)
            }
        })
        animator.start()
    }

    /**
     * innerBounds 在 initBounds 居中且大小是一半
     * 如果 content bounds 比 initBounds 大，则最大偏移距离不能超过 innerBounds
     * 如果 content bounds 大小 initBounds 和 innerBounds 之间，则居中显示
     * 如果 content bounds 小于 innerBounds 则放大到 innerBounds 剧中
     */
    private fun generateToRect(): RectF? {
        val bounds = mCoreService?.getContentBounds() ?: return null
        val initBounds = mCoreService?.getInitBounds() ?: return null
        //innerBounds 是 initBounds 长宽的一半，且居中
        innerBounds.set(initBounds)
        innerBounds.inset(innerBounds.width() / 4f, innerBounds.height() / 4f)
        val to = RectF(bounds)
        //如果 to 比 innerBounds 小，则放大到 innerBounds
        if (to.height() < innerBounds.height()) {
            to.set(innerBounds)
            return to
        }
        //如果 to 比 initBounds 小，则居中
        if (to.height() < initBounds.height()) {
            val left = initBounds.left + (initBounds.width() - to.width()) / 2
            val top = initBounds.top + (initBounds.height() - to.height()) / 2
            to.set(left, top, left + to.width(), top + to.height())
            return to
        }
        var offsetX = 0f
        var offsetY = 0f

        //如果 to 比 initBounds 大，则限定移动边界不能超过 innerBounds
        if (to.height() >= initBounds.height()) {
            if (to.right < innerBounds.right) {
                offsetX = innerBounds.right - to.right
            } else if (to.left > innerBounds.left) {
                offsetX = innerBounds.left - to.left
            }
            if (to.bottom < innerBounds.bottom) {
                offsetY = innerBounds.bottom - to.bottom
            } else if (to.top > innerBounds.top) {
                offsetY = innerBounds.top - to.top
            }
        }

        if (offsetX != 0f || offsetY != 0f) {
            to.offset(offsetX, offsetY)
            return to
        }
        return null
    }

    override fun onTransformStart(point: PointF, point2: PointF): Boolean {
        //如果双指中点不在画面内容里，
        val center = PointF((point.x + point2.x) / 2f, (point.y + point2.y) / 2f)
        scaleAndTranslate = mCoreService?.getContentBounds()?.contains(center) ?: false
        return false
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
        val scaleLimit = scaleX > 6f && scale > 1f
        if (scaleAndTranslate) {
            if (!scaleLimit) {
                transformMatrix.postScale(scale, scale, (x1 + x2) / 2f, (y1 + y2) / 2f)
            }
            val dx = (x1 + x2) / 2f - (lastPoint.x + lastPoint2.x) / 2f
            val dy = (y1 + y2) / 2f - (lastPoint.y + lastPoint2.y) / 2f

            transformMatrix.postTranslate(dx, dy)
        } else {
            if (!scaleLimit) {
                mCoreService?.getContentBounds()?.let {
                    transformMatrix.postScale(scale, scale, it.centerX(), it.centerY())
                }
            }
        }

        mCoreService?.setTransformMatrix(transformMatrix)
        return false
    }

    override fun onTransformEnd(): Boolean {
        tryKeepInInnerBounds()
        return false
    }
}

interface ITransformService : IService {
    fun tryKeepInInnerBounds()
}