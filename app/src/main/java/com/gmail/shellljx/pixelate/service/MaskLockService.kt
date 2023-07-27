package com.gmail.shellljx.pixelate.service

import android.graphics.*
import android.os.*
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Keep
import androidx.core.graphics.toRect
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.gmail.shellljx.pixelate.BasicAuthInterceptor
import com.gmail.shellljx.pixelate.extension.launch
import com.gmail.shellljx.pixelate.extension.writeToPngFile
import com.gmail.shellljx.pixelate.panel.ProgressPanel
import com.gmail.shellljx.pixelate.utils.BitmapUtils
import com.gmail.shellljx.pixelate.view.MaskRenderView
import com.gmail.shellljx.pixelator.MaskMode
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.*
import com.gmail.shellljx.wrapper.service.panel.PanelToken
import com.gmail.shellljx.wrapper.service.render.IRenderLayer
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/7
 * @Description:
 */
@Keep
class MaskLockService : IMaskLockService, LifecycleObserver, OnImageLoadedObserver, OnContentBoundsObserver, OnTapObserver {
    companion object {
        private const val PATH_SMALL_SRC = "/assets/small/"
        private const val PATH_REMOVED_BG = "/assets/removed/"
    }

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var mMaskRenderView: MaskRenderView? = null
    private var mProgressToken: PanelToken? = null
    @MaskMode
    private var mMaskMode = MaskMode.NONE
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(BasicAuthInterceptor())
            .callTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    override fun onStart() {
        mContainer.getLifeCycleService()?.addObserver(this)
        mCoreService?.addContentBoundsObserver(this)
        mContainer.getGestureService()?.addTapObserver(this)
        mCoreService?.addImageLoadedObserver(this)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onStop() {
        mContainer.getLifeCycleService()?.removeObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onLifecycleResume() {
        if (mMaskRenderView == null) {
            val maskView = MaskRenderView(mContainer.getContext())
            mMaskRenderView = maskView
            mContainer.getRenderService()?.addRenderLayer(object : IRenderLayer {
                override fun view(): View {
                    return maskView
                }

                override fun level(): Int {
                    return 0
                }
            })
        }
    }

    override fun setMaskMode(mode: Int) {
        if (mMaskRenderView?.getMaskBitmap() == null && mode != MaskMode.NONE) {
            makeRemoveBackgroundMask()
        } else {
            mMaskRenderView?.setMaskMode(mode)
            mCoreService?.setDeeplabMode(mode)
        }
        mMaskMode = mode
    }

    override fun getMaskMode(): Int {
        return mMaskMode
    }

    private fun makeRemoveBackgroundMask() {
        val path = mCoreService?.getImagePath() ?: return
        launch(mContainer) {
            mProgressToken = mContainer.getPanelService()?.showPanel(ProgressPanel::class.java)
            val fileName = getFileName(path)
            val removedBgPath = mContainer.getContext().cacheDir.absolutePath + PATH_REMOVED_BG + fileName + ".png"
            if (!File(removedBgPath).exists()) {
                //文件不存在
                val scalePath = mContainer.getContext().cacheDir.absolutePath + PATH_SMALL_SRC + fileName + ".png"
                val scaledBitmap = BitmapUtils.decodeBitmap(path, 4000)
                scaledBitmap.writeToPngFile(scalePath, 50)
                requestRemoveBackground(scalePath, removedBgPath)
            }
            val maskBitmap = BitmapUtils.decodeBitmap(removedBgPath, 4000)
            setMaskBitmap(maskBitmap)
            mProgressToken?.let {
                mContainer.getPanelService()?.hidePanel(it)
            }
        }
    }

    private suspend fun requestRemoveBackground(srcPath: String, dstPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "origin_image.png", RequestBody.create("image/png".toMediaTypeOrNull(), File(srcPath)))
                .build()
            val request: Request = Request.Builder()
                .url("https://api.pixian.ai/api/v2/remove-background")
                .post(requestBody)
                .build()
            val targetFile = File(dstPath)
            if (targetFile.parentFile?.exists() != true) {
                targetFile.parentFile?.mkdirs()
            }
            val fileOutputStream = FileOutputStream(targetFile)
            try {
                val response = client.newCall(request).execute()
                if (!isActive) {
                    return@withContext false
                }
                val buff = response.body?.bytes()
                fileOutputStream.write(buff)
                if (!isActive) {
                    return@withContext false
                }
                true
            } catch (e: Exception) {
                false
            } finally {
                fileOutputStream.close()
            }
        }
    }

    private fun getFileName(path: String): String {
        val md = MessageDigest.getInstance("MD5")
        val messageDigest = md.digest(path.toByteArray())
        val no = BigInteger(1, messageDigest)
        var hashText = no.toString(16)
        while (hashText.length < 32) {
            hashText = "0$hashText"
        }
        return hashText
    }

    private fun setMaskBitmap(bitmap: Bitmap) {
        mCoreService?.setDeeplabMask(bitmap)
        mCoreService?.setDeeplabMode(mMaskMode)
        val bounds = mCoreService?.getContentBounds() ?: return
        mMaskRenderView?.setMask(bounds.toRect(), bitmap)
        mMaskRenderView?.setMaskMode(mMaskMode)
    }

    override fun onContentBoundsChanged(bound: Rect) {
        val bounds = mCoreService?.getContentBounds() ?: return
        mMaskRenderView?.setContentBounds(bounds.toRect())
    }

    override fun onSingleDown(event: MotionEvent): Boolean {
        if (mMaskRenderView?.isAnimating() == true) {
            return false
        }
        mMaskRenderView?.isVisible = true
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        if (mMaskRenderView?.isAnimating() == true) {
            return false
        }
        mMaskRenderView?.isVisible = false
        return false
    }

    override fun onImageLoaded(path: String) {

    }
}

interface IMaskLockService : IService {
    fun setMaskMode(@MaskMode mode: Int)
    @MaskMode
    fun getMaskMode(): Int
}