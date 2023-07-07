package com.gmail.shellljx.pixelate.service

import android.animation.ValueAnimator
import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.EffectsPanel
import com.gmail.shellljx.pixelator.EffectType
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.gesture.OnTapObserver
import com.gmail.shellljx.wrapper.service.panel.PanelToken

class EffectService : IEffectService, OnTapObserver {
    private lateinit var mContainer: IContainer
    private val effectList = arrayListOf<EffectItem>()
    private val effectChangedObservers = arrayListOf<EffectChangedObserver>()
    private var mCoreService: IPixelatorCoreService? = null
    private var mPanelToken: PanelToken? = null
    private var mViewPortAnimator = ValueAnimator.ofInt(200.dp(), 0)
    override fun onStart() {
        effectList.add(EffectItem(0, EffectType.TypeMosaic, "/sdcard/PixelatorResources/0.jpg", ""))
        effectList.add(EffectItem(1, EffectType.TypeImage, "/sdcard/PixelatorResources/1.jpg", "/sdcard/PixelatorResources/1.jpg"))
        effectList.add(EffectItem(2, EffectType.TypeImage, "/sdcard/PixelatorResources/2.png", "/sdcard/PixelatorResources/2.png"))
        effectList.add(EffectItem(3, EffectType.TypeImage, "/sdcard/PixelatorResources/3.png", "/sdcard/PixelatorResources/3.png"))
        effectList.add(EffectItem(4, EffectType.TypeImage, "/sdcard/PixelatorResources/4.png", "/sdcard/PixelatorResources/4.png"))
        effectList.add(EffectItem(5, EffectType.TypeImage, "/sdcard/PixelatorResources/5.png", "/sdcard/PixelatorResources/5.png"))
        effectList.add(EffectItem(6, EffectType.TypeImage, "/sdcard/PixelatorResources/6.png", "/sdcard/PixelatorResources/6.png"))
        effectList.add(EffectItem(7, EffectType.TypeImage, "/sdcard/PixelatorResources/7.png", "/sdcard/PixelatorResources/7.png"))
        effectList.add(EffectItem(8, EffectType.TypeImage, "/sdcard/PixelatorResources/8.png", "/sdcard/PixelatorResources/8.png"))
        effectList.add(EffectItem(9, EffectType.TypeImage, "/sdcard/PixelatorResources/9.png", "/sdcard/PixelatorResources/9.png"))
        effectList.add(EffectItem(10, EffectType.TypeImage, "/sdcard/PixelatorResources/10.png", "/sdcard/PixelatorResources/10.png"))
        effectList.add(EffectItem(11, EffectType.TypeImage, "/sdcard/PixelatorResources/11.jpg", "/sdcard/PixelatorResources/11.jpg"))
        effectList.add(EffectItem(12, EffectType.TypeImage, "/sdcard/PixelatorResources/12.png", "/sdcard/PixelatorResources/12.png"))
        effectList.add(EffectItem(12, EffectType.TypeImage, "/sdcard/PixelatorResources/13.png", "/sdcard/PixelatorResources/13.png"))
        mContainer.getGestureService()?.addTapObserver(this)
        mCoreService?.updateViewPort(200.dp())
        mViewPortAnimator.duration = 400
        mViewPortAnimator.addUpdateListener(mAnimatorUpdateListener)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
        mContainer.getRenderService()?.getRenderHeight()
    }

    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener {
        val viewport = it.animatedValue as Int
        mCoreService?.updateViewPort(viewport)
    }

    override fun onStop() {
        mViewPortAnimator.cancel()
        mViewPortAnimator.removeAllUpdateListeners()
        mViewPortAnimator.removeAllListeners()
    }

    override fun onDoubleTap(): Boolean {
        if (mViewPortAnimator.isRunning) return false
        mPanelToken?.let {
            if (it.isAttach == true) {
                mContainer.getPanelService()?.hidePanel(it)
                tryUpdateViewPort(false)
            } else {
                mContainer.getPanelService()?.showPanel(it)
                tryUpdateViewPort(true)
            }
        }
        return false
    }

    private fun tryUpdateViewPort(reverse: Boolean) {
        val renderWidth = mContainer.getRenderService()?.getRenderWidth() ?: return
        val renderHeight = mContainer.getRenderService()?.getRenderHeight() ?: return
        val contentBounds = mCoreService?.getContentBounds() ?: return
        if (contentBounds.width() < renderWidth || contentBounds.height() < renderHeight) {
            if (reverse) {
                mViewPortAnimator.reverse()
            } else {
                mViewPortAnimator.start()
            }
        }
    }

    override fun showPanel() {
        mPanelToken = mContainer.getPanelService()?.showPanel(EffectsPanel::class.java)
    }

    override fun getEffects(): List<EffectItem> {
        return effectList
    }

    override fun addEffectChangedObserver(observer: EffectChangedObserver) {
        effectChangedObservers.add(observer)
    }
}

interface IEffectService : IService {
    fun showPanel()
    fun getEffects(): List<EffectItem>
    fun addEffectChangedObserver(observer: EffectChangedObserver)
}

interface EffectChangedObserver {
    fun onEffectChanged(effects: List<EffectItem>)
}