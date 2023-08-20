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
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.writeToPngFile
import com.gmail.shellljx.pixelate.panel.ProgressPanel
import com.gmail.shellljx.pixelate.utils.BitmapUtils
import com.gmail.shellljx.pixelate.view.MaskRenderView
import com.gmail.shellljx.pixelator.MaskMode
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.AbsService
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
class MaskLockService(container: IContainer) : AbsService(container), IMaskLockService, LifecycleObserver, OnContentBoundsObserver, OnTapObserver, MaskModeObserver {
    companion object {
        private const val PATH_SMALL_SRC = "/assets/small/"
        private const val PATH_REMOVED_BG = "/assets/removed/"
    }

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var mMaskRenderView: MaskRenderView? = null
    private var mProgressToken: PanelToken? = null
    private var mMaskSourceImage: String? = null

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
        lifecycle.addObserver(this)
        mCoreService?.addContentBoundsObserver(this)
        mCoreService?.addMaskModeObserver(this)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
    }

    override fun onStop() {
        lifecycle.removeObserver(this)
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
        if (mMaskSourceImage != mCoreService?.getImagePath() && mode != MaskMode.NONE) {
            makeRemoveBackgroundMask(mode)
        } else {
            mMaskRenderView?.setMaskMode(mode)
            mCoreService?.setDeeplabMode(mode)
        }
    }

    private fun makeRemoveBackgroundMask(@MaskMode mode: Int) {
        val path = mCoreService?.getImagePath() ?: return
        lifecycleScope.launch {
            val message = mContainer.getContext().getString(R.string.mask_uploading)
            mProgressToken = mContainer.getPanelService()?.showPanel(ProgressPanel::class.java, message)
            val fileName = getFileName(path)
            val removedBgPath = mContainer.getContext().cacheDir.absolutePath + PATH_REMOVED_BG + fileName + ".png"
            if (!File(removedBgPath).exists()) {
                //文件不存在
                val scalePath = mContainer.getContext().cacheDir.absolutePath + PATH_SMALL_SRC + fileName + ".jpg"
                val scaledBitmap = BitmapUtils.decodeBitmap(path, 1024)
                scaledBitmap.writeToPngFile(scalePath, 50)
                requestRemoveBackground(scalePath, removedBgPath)
            }
            val maskBitmap = BitmapUtils.decodeBitmap(removedBgPath, 1024)
            setMaskBitmap(maskBitmap, mode)
            mMaskSourceImage = path
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

    private fun setMaskBitmap(bitmap: Bitmap, @MaskMode mode: Int) {
        mCoreService?.setDeeplabMask(bitmap)
        mCoreService?.setDeeplabMode(mode)
        val bounds = mCoreService?.getContentBounds() ?: return
        mMaskRenderView?.setMask(bounds.toRect(), bitmap)
        mMaskRenderView?.setMaskMode(mode)
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

    override fun onMaskModeChanged(mode: Int) {
        if (mode == MaskMode.NONE) {
            mContainer.getGestureService()?.removeTapObserver(this)
        } else {
            mContainer.getGestureService()?.addTapObserver(this)
        }
    }
}

interface IMaskLockService : IService {
    fun setMaskMode(@MaskMode mode: Int)
}