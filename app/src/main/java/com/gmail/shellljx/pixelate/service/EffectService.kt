package com.gmail.shellljx.pixelate.service

import android.animation.*
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.MosaicPanel
import com.gmail.shellljx.pixelate.view.PaintBoxView
import com.gmail.shellljx.pixelate.viewmodel.EffectViewModel
import com.gmail.shellljx.pixelate.widget.WidgetEvents
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.extension.viewModels
import com.gmail.shellljx.wrapper.service.AbsService
import com.gmail.shellljx.wrapper.service.gesture.OnSingleMoveObserver
import com.gmail.shellljx.wrapper.service.gesture.OnTapObserver
import com.gmail.shellljx.wrapper.service.panel.PanelToken
import com.gmail.shellljx.wrapper.service.render.IRenderLayer
import org.json.JSONObject

@Keep
class EffectService(container: IContainer) : AbsService(container), IEffectService, OnTapObserver,
    OnSingleMoveObserver {
    private val effectViewModel: EffectViewModel by viewModels()
    private var mCoreService: IPixelatorCoreService? = null
    private var mTransformService: ITransformService? = null
    private var mPanelToken: PanelToken? = null
    private var mViewPortAnimator = ValueAnimator.ofInt(200.dp(), 0)
    private var mDrawBoxView: PaintBoxView? = null
    private val mAssetPath by lazy { container.getContext().cacheDir.absolutePath + "/assets/assets.json" }

    private val mDrawBoxLayer = object : IRenderLayer {
        override fun view(): View {
            var view = mDrawBoxView
            if (view == null) {
                view = PaintBoxView(container.getContext())
                mDrawBoxView = view
            }
            return view
        }

        override fun level(): Int {
            return 0
        }
    }

    override fun onStart() {
        effectViewModel.fetch(mAssetPath)
        container.getGestureService()?.addTapObserver(this)
        mCoreService?.updateViewPort(80.dp())
        mViewPortAnimator.duration = 400
        mViewPortAnimator.addUpdateListener(mAnimatorUpdateListener)
        mViewPortAnimator.addListener(mAnimatorListener)
    }

    override fun bindVEContainer(container: IContainer) {
        mCoreService = container.getServiceManager().getService(PixelatorCoreService::class.java)
        mTransformService = container.getServiceManager().getService(TransformService::class.java)
        container.getRenderService()?.getRenderHeight()
        container.getGestureService()?.addSingleMoveObserver(this)
    }

    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
        val viewport = it.animatedValue as Int
        mCoreService?.updateViewPort(viewport)
        val progress = 1 - (viewport.toFloat() / 200.dp())
        container.getControlService()
            ?.sendWidgetMessage(WidgetEvents.MSG_TRANSLATE_PROGRESS, progress)
    }

    private val mAnimatorListener = object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            mTransformService?.tryKeepInInnerBounds()
        }
    }

    override fun onStop() {
        mViewPortAnimator.cancel()
        mViewPortAnimator.removeAllUpdateListeners()
        mViewPortAnimator.removeAllListeners()
    }

    override fun applyEffect(item: EffectItem) {
        val effectObj = JSONObject()
        effectObj.put("type", item.type)
        val configObj = JSONObject()
        configObj.put("rectSize", 50)
        configObj.put("url", item.path)
        effectObj.put("config", configObj)
        val effectStr = effectObj.toString()
        mCoreService?.setEffect(effectStr)
    }

    override fun onDoubleTap(): Boolean {
        if (mViewPortAnimator.isRunning) return false
        mPanelToken?.let {
            if (it.isAttach == true) {
                container.getPanelService()?.hidePanel(it)
                tryUpdateViewPort(false)
            } else {
                container.getPanelService()?.showPanel(it)
                tryUpdateViewPort(true)
            }
        }
        return false
    }

    override fun onSingleDown(event: MotionEvent): Boolean {
        if (mDrawBoxView?.isAttachedToWindow == true) {
            mDrawBoxView?.setStartPoint(event.x, event.y)
        }
        return false
    }

    override fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF): Boolean {
        if (mDrawBoxView?.isAttachedToWindow == true) {
            mDrawBoxView?.setEndPoint(current.x, current.y)
        }
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        if (mDrawBoxView?.isAttachedToWindow == true) {
            mDrawBoxView?.clear()
        }
        return false
    }

    private fun tryUpdateViewPort(reverse: Boolean) {
        if (reverse) {
            mViewPortAnimator.reverse()
        } else {
            mViewPortAnimator.start()
        }
    }

    override fun showPanel() {
        mPanelToken = container.getPanelService()?.showPanel(MosaicPanel::class.java)
    }

    override fun addDrawBox() {
        container.getRenderService()?.addRenderLayer(mDrawBoxLayer)
    }

    override fun removDrawBox() {
        container.getRenderService()?.removeRenderLayer(mDrawBoxLayer)
    }
}

interface IEffectService : IService {
    fun showPanel()
    fun addDrawBox()
    fun removDrawBox()
    fun applyEffect(item: EffectItem)
}