package com.gmail.shellljx.pixelate.service

import android.content.Context
import android.graphics.*
import android.os.*
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.gmail.shellljx.pixelate.BasicAuthInterceptor
import com.gmail.shellljx.pixelate.view.DeeplabMaskView
import com.gmail.shellljx.pixelator.MaskMode
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.OnSingleDownObserver
import com.gmail.shellljx.wrapper.service.gesture.OnSingleUpObserver
import com.gmail.shellljx.wrapper.service.render.IRenderLayer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.WeakReference

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/7
 * @Description:
 */
class Deeplabv3Service : IDeeplabv3Service, LifecycleObserver, OnImageLoadedObserver, OnDeeplabMaskObserver, OnContentBoundsObserver, OnSingleUpObserver, OnSingleDownObserver {
    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null
    private var worker: HandlerThread? = null // 工作线程（加载和运行模型）
    private var receiver: ReceiverHander? = null // 接收来自工作线程的消息
    private var sender: Handler? = null // 发送消息给工作线程
    private var mDeeplabMaskView: DeeplabMaskView? = null

    override fun onStart() {
        mContainer.getLifeCycleService()?.addObserver(this)
        mCoreService?.addDeeplabMaskObserver(this)
        mCoreService?.addContentBoundsObserver(this)
        mContainer.getGestureService()?.addSingleUpObserver(this)
        mContainer.getGestureService()?.addSingleDownObserver(this)
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
        if (mDeeplabMaskView == null) {
            val maskView = DeeplabMaskView(mContainer.getContext())
            mDeeplabMaskView = maskView
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

    private fun setMaskInternal(bitmap: Bitmap) {
        mCoreService?.setDeeplabMask(bitmap)
        mCoreService?.setDeeplabMode(MaskMode.PERSON)
    }

    private fun dispatchRunModeSuccess(bitmap: Bitmap) {
        val msg = Message.obtain()
        msg.what = ReceiverHander.RUN_MODEL_SUCCESSED
        msg.obj = bitmap
        receiver?.sendMessage(msg)
    }

    override fun onDeeplabMaskChanged(mask: Bitmap) {
        val bounds = mCoreService?.getContentBounds() ?: return
        mDeeplabMaskView?.setMask(bounds.toRect(), mask)
    }

    override fun onContentBoundsChanged(bound: Rect) {
        val bounds = mCoreService?.getContentBounds() ?: return
        mDeeplabMaskView?.setContentBounds(bounds.toRect())
    }

    override fun onSingleDown(event: MotionEvent): Boolean {
        mDeeplabMaskView?.isVisible = true
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        mDeeplabMaskView?.isVisible = false
        return false
    }

    override fun onImageLoaded(path: String) {
        if (receiver == null) {
            receiver = ReceiverHander(WeakReference(this))
        }
        if (worker == null) {
            worker = HandlerThread("Thread_Predictor_Worker")
            worker?.start()
        }
        if (sender == null) {
            worker?.let { sender = SenderHandler(WeakReference(this), WeakReference(mContainer.getContext()), it.looper) }
            sender?.sendEmptyMessage(SenderHandler.REQUEST_INIT)
        }
        val msg = Message.obtain()
        msg.what = SenderHandler.REQUEST_DETECT
        msg.obj = path
        sender?.sendMessage(msg)
    }

    private class ReceiverHander(private val service: WeakReference<Deeplabv3Service>) : Handler(Looper.getMainLooper()) {
        companion object {
            const val RUN_MODEL_SUCCESSED = 0
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                RUN_MODEL_SUCCESSED -> {
                    service.get()?.setMaskInternal(msg.obj as Bitmap)
                }
            }
        }
    }

    private class SenderHandler(
        private val service: WeakReference<Deeplabv3Service>,
        private val context: WeakReference<Context>, worker: Looper
    ) : Handler(worker) {
        private var client: OkHttpClient? = null

        companion object {
            const val REQUEST_INIT = 0
            const val REQUEST_DETECT = 1
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REQUEST_INIT -> {
                    client = OkHttpClient.Builder()
                        .addInterceptor(BasicAuthInterceptor()).build()
                }

                REQUEST_DETECT -> {
                    val bitmap = removeBackground(msg.obj as String) ?: return
                    service.get()?.dispatchRunModeSuccess(bitmap)
                }
            }
        }

        private fun removeBackground(path: String?): Bitmap? {
            path ?: return null
            val smallpath = getminiPath(path) ?: return null
            val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image", "origin_image.png", RequestBody.create("image/png".toMediaTypeOrNull(), File(smallpath)))
                .build()
            val request: Request = Request.Builder()
                .url("https://api.pixian.ai/api/v2/remove-background")
                .post(requestBody)
                .build()
            val response = client?.newCall(request)?.execute()
            val out = FileOutputStream(
                (context.get()?.filesDir?.absolutePath
                    ?: "") + "/" + "removed.png"
            )
            val buff = response?.body?.bytes()
            out.write(buff)
            out.close()
            val bitmap = BitmapFactory.decodeFile(
                (context.get()?.filesDir?.absolutePath
                    ?: "") + "/" + "removed.png"
            )
            return bitmap
        }

        private fun getminiPath(path: String?): String? {
            path ?: return null
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            val scaleFactor = Math.max(originalWidth / 720, originalHeight / 720)
            options.inJustDecodeBounds = false
            options.inSampleSize = scaleFactor
            val scaledBitmap = BitmapFactory.decodeFile(path, options)
            val small = File(
                (context.get()?.filesDir?.absolutePath
                    ?: "") + "/" + "small.jpg"
            )
            val stream = FileOutputStream(small)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            return small.absolutePath
        }
    }
}

interface IDeeplabv3Service : IService {
}