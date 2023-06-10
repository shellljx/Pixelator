package com.gmail.shellljx.pixelate.service

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toRect
import androidx.core.view.isVisible
import androidx.lifecycle.*
import com.gmail.shellljx.pixelate.view.DeeplabMaskView
import com.gmail.shellljx.pixelator.MaskMode
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.OnSingleDownObserver
import com.gmail.shellljx.wrapper.service.gesture.OnSingleUpObserver
import com.gmail.shellljx.wrapper.service.render.IRenderLayer
import com.tencent.deeplabv3plus.Deeplabv3plusNcnn
import java.io.IOException
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
        companion object {
            const val REQUEST_INIT = 0
            const val REQUEST_DETECT = 1
        }

        private var deeplabv3: Deeplabv3plusNcnn? = null

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                REQUEST_INIT -> {
                    if (deeplabv3 == null) {
                        deeplabv3 = Deeplabv3plusNcnn()
                        deeplabv3?.Init(context.get()?.assets)
                    }
                }

                REQUEST_DETECT -> {
                    val bitmap = decodeUri(msg.obj as String) ?: return
                    val output: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    deeplabv3?.Detect(bitmap, output, false)
                    service.get()?.dispatchRunModeSuccess(output)
                }
            }
        }

        private fun decodeUri(selectedImage: String?): Bitmap? {
            selectedImage ?: return null
            // Decode image size
            val o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeFile(selectedImage, o)

            // The new size we want to scale to
            val REQUIRED_SIZE = 640

            // Find the correct scale value. It should be the power of 2.
            var width_tmp = o.outWidth
            var height_tmp = o.outHeight
            var scale = 1
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                    break
                }
                width_tmp /= 2
                height_tmp /= 2
                scale *= 2
            }

            // Decode with inSampleSize
            val o2 = BitmapFactory.Options()
            o2.inSampleSize = scale
            val bitmap = BitmapFactory.decodeFile(selectedImage, o2)

            // Rotate according to EXIF
            var rotate = 0
            try {
                val exif: ExifInterface = ExifInterface(selectedImage)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "ExifInterface IOException")
            }
            val matrix = Matrix()
            matrix.postRotate(rotate.toFloat())
            return Bitmap.createBitmap(bitmap!!, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }
    }
}

interface IDeeplabv3Service : IService {
}