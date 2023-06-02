package com.gmail.shellljx.wrapper.service.core

import android.graphics.*
import android.media.ExifInterface
import android.view.Surface
import androidx.annotation.RawRes
import com.gmail.shellljx.pixelator.*
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.OnSingleMoveObserver
import com.gmail.shellljx.wrapper.service.gesture.OnTransformObserver
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService
import com.gmail.shellljx.wrapper.service.render.RenderContainerService
import com.gmail.shellljx.wrapper.utils.PointUtils
import java.util.LinkedList

class PixelatorCoreService : IPixelatorCoreService, IRenderContext, OnSingleMoveObserver, OnTransformObserver {
    private lateinit var mContainer: IContainer
    private var mRenderService: IRenderContainerService? = null

    private lateinit var mImageSdk: IPixelator
    private val mPenddingTasks = LinkedList<Runnable>()
    private val mContentBounds = RectF()
    private var mEglWindowCreated = false

    private val mRenderListener = object : IRenderListener {
        override fun onEGLContextCreate() {

        }

        override fun onEGLWindowCreate() {
            mEglWindowCreated = true
            mPenddingTasks.forEach {
                it.run()
            }
            val path = "/data/data/com.gmail.shellljx.pixelate/deer-8008410_1280.jpeg"
            mImageSdk.addImagePath(path, getRotate(path))
            mPenddingTasks.clear()
            mImageSdk.refreshFrame()
        }

        override fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
            mContentBounds.set(left, top, right, bottom)
        }

        override fun onFrameSaved(bitmap: Bitmap) {
        }

    }

    override fun onStart() {
        mRenderService?.bindRenderContext(this)
        mImageSdk = Pixelator.create()
        mImageSdk.setRenderListener(mRenderListener)
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

    override fun setBrushResource(id: Int) {

    }

    override fun getMiniScreen(): IMiniScreen {
        return mImageSdk.getMiniScreen()
    }

    override fun getContentBounds(): RectF {
        return mContentBounds
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
        return false
    }

    override fun onTransform(matrix: Matrix): Boolean {
        return true
    }
}

interface IPixelatorCoreService : IService {

    fun setBrushResource(@RawRes id: Int)
    fun getMiniScreen(): IMiniScreen
    fun getContentBounds(): RectF
}