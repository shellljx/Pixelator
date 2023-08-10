package com.gmail.shellljx.pixelate.service

import android.animation.*
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.lifecycle.lifecycleScope
import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.EffectsPanel
import com.gmail.shellljx.pixelate.view.PaintBoxView
import com.gmail.shellljx.pixelate.widget.WidgetEvents
import com.gmail.shellljx.pixelator.EffectType
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.AbsService
import com.gmail.shellljx.wrapper.service.gesture.OnSingleMoveObserver
import com.gmail.shellljx.wrapper.service.gesture.OnTapObserver
import com.gmail.shellljx.wrapper.service.panel.PanelToken
import com.gmail.shellljx.wrapper.service.render.IRenderLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.FileOutputStream
import java.lang.Exception

@Keep
class EffectService(container: IContainer) : AbsService(container), IEffectService, OnTapObserver, OnSingleMoveObserver {
    private val effectList = arrayListOf<EffectItem>()
    private val effectChangedObservers = arrayListOf<EffectChangedObserver>()
    private var mCoreService: IPixelatorCoreService? = null
    private var mTransformService: ITransformService? = null
    private var mPanelToken: PanelToken? = null
    private var mViewPortAnimator = ValueAnimator.ofInt(200.dp(), 0)
    private var mDrawBoxView: PaintBoxView? = null

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
        lifecycleScope.launch {
            downloadFile("https://storage.googleapis.com/app_assetss/assets.json", container.getContext().cacheDir.absolutePath + "/cache/assets.json")
        }
        effectList.add(EffectItem(0, EffectType.TypeMosaic, "/sdcard/Pictures/Pixelator/IMG_20230809_213805.jpg", ""))
        effectList.add(EffectItem(1, EffectType.TypeImage, "/sdcard/PixelatorResources/1-fotor-2023080921446.jpg", "/sdcard/PixelatorResources/1.jpg"))
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
        effectList.add(EffectItem(13, EffectType.TypeImage, "/sdcard/PixelatorResources/20.jpeg", "/sdcard/PixelatorResources/20.jpeg"))
        container.getGestureService()?.addTapObserver(this)
        mCoreService?.updateViewPort(200.dp())
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
        container.getControlService()?.sendWidgetMessage(WidgetEvents.MSG_TRANSLATE_PROGRESS, progress)
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

    private suspend fun downloadFile(url: String, destination: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val request = Request.Builder()
                        .url(url)
                        .build()

                val response = client.newCall(request).execute()
                val inputStream = response.body?.byteStream()

                val fileOutputStream = FileOutputStream(destination)
                val buffer = ByteArray(4096)
                var bytesRead: Int

                inputStream?.use { input ->
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead)
                    }
                }

                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            destination
        }

    }

    override fun showPanel() {
        mPanelToken = container.getPanelService()?.showPanel(EffectsPanel::class.java)
    }

    override fun addDrawBox() {
        container.getRenderService()?.addRenderLayer(mDrawBoxLayer)
    }

    override fun removDrawBox() {
        container.getRenderService()?.removeRenderLayer(mDrawBoxLayer)
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
    fun addDrawBox()
    fun removDrawBox()
    fun getEffects(): List<EffectItem>
    fun addEffectChangedObserver(observer: EffectChangedObserver)
}

interface EffectChangedObserver {
    fun onEffectChanged(effects: List<EffectItem>)
}