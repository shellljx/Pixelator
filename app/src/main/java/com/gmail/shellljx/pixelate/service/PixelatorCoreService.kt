package com.gmail.shellljx.pixelate.service

import android.graphics.*
import android.media.ExifInterface
import android.view.Surface
import com.gmail.shellljx.pixelate.PointUtils
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelator.*
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.OnSingleMoveObserver
import com.gmail.shellljx.wrapper.service.gesture.OnTransformObserver
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService
import com.gmail.shellljx.wrapper.service.render.RenderContainerService

class PixelatorCoreService : IPixelatorCoreService, IRenderContext, IRenderListener, OnSingleMoveObserver, OnTransformObserver {
    private lateinit var mContainer: IContainer
    private var mRenderService: IRenderContainerService? = null

    private lateinit var mImageSdk: IPixelator

    override fun onStart() {
        mRenderService?.bindRenderContext(this)
        mImageSdk = Pixelator.create()
        mImageSdk.setRenderListener(this)
        mContainer.getGestureService()?.addSingleMoveObserver(this)
        mContainer.getGestureService()?.addTransformObserver(this)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mRenderService = mContainer.getServiceManager().getService(RenderContainerService::class.java)
    }

    override fun onStop() {
    }

    override fun setDisplaySuerface(surface: Surface) {
        mImageSdk.setDisplaySurface(surface)
    }

    override fun updateSurfaceChanged(width: Int, height: Int) {
        mImageSdk.viewPortChanged(width, height)
    }

    override fun destroyDisplaySurface() {
        mImageSdk.destroyDisplaySurface()
    }

    override fun onEGLContextCreate() {

    }

    override fun onEGLWindowCreate() {
        val bitmap = BitmapFactory.decodeResource(mContainer.getContext().resources, R.mipmap.ic_brush_blur)
        mImageSdk.setBrush(bitmap)
        bitmap.recycle()
        val path = "/sdcard/aftereffect/ae/asset11.png"
        mImageSdk.addImagePath(path, getRotate(path))
        mImageSdk.refreshFrame()

//        surfaceView.post {
//            serviceManager.miniScreenPanel.onCreateView(findViewById(R.id.layout_miniscreen))
//        }
    }

    override fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
    }

    override fun onFrameSaved(bitmap: Bitmap) {
    }

    private fun getRotate(path: String): Int {
        return try {
            val exifInterface = ExifInterface(path)
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    override fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF): Boolean {
        val points = PointUtils.pointsWith(from, to, control, 50f)
        val buffer = arrayListOf<Float>()
        points.forEach {
            buffer.add(it.x)
            buffer.add(it.y)
        }
        mImageSdk.pushTouchBuffer(buffer.toFloatArray(), current.x, current.y)
        mImageSdk.refreshFrame()
        return true
    }

    override fun onTransform(matrix: Matrix): Boolean {
        return true
    }
}

interface IPixelatorCoreService : IService {

}