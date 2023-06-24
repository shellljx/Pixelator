package com.gmail.shellljx.pixelate.service

import android.graphics.*
import android.media.ExifInterface
import android.view.MotionEvent
import android.view.Surface
import androidx.core.graphics.toRect
import com.gmail.shellljx.pixelator.*
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.OnSingleMoveObserver
import com.gmail.shellljx.wrapper.service.gesture.OnSingleUpObserver
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService
import com.gmail.shellljx.wrapper.service.render.RenderContainerService
import com.gmail.shellljx.wrapper.utils.PointUtils
import java.util.LinkedList

class PixelatorCoreService : IPixelatorCoreService, IRenderContext, OnSingleMoveObserver, OnSingleUpObserver {
    private lateinit var mContainer: IContainer
    private var mRenderService: IRenderContainerService? = null

    private lateinit var mImageSdk: IPixelator
    private val mPenddingTasks = LinkedList<Runnable>()
    private val mInitBounds = RectF() //图片插入成功初始 bounds
    private val mContentBounds = RectF() //图片变换后最新的bounds
    private val mTransformMatrix = Matrix() //变换矩阵
    private var mPaintSize = 0

    @PaintType
    private var mPaintType = PAINT
    private var mEglWindowCreated = false
    private val mPaintSizeObservers = arrayListOf<PaintSizeObserver>()
    private val mDeeplabMaskObservers = arrayListOf<OnDeeplabMaskObserver>()
    private val mContentBoundsObservers = arrayListOf<OnContentBoundsObserver>()
    private val mImageLoadedObservers = arrayListOf<OnImageLoadedObserver>()

    private val mRenderListener = object : IRenderListener {
        override fun onEGLContextCreate() {

        }

        override fun onEGLWindowCreate() {
            mEglWindowCreated = true
            mPenddingTasks.forEach {
                it.run()
            }
            mPenddingTasks.clear()
        }

        override fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
            if (mInitBounds.isEmpty) {
                mInitBounds.set(left, top, right, bottom)
            }
            mContentBounds.set(left, top, right, bottom)
            mContentBoundsObservers.forEach { it.onContentBoundsChanged(mContentBounds.toRect()) }
        }

        override fun onFrameSaved(bitmap: Bitmap) {
        }

        override fun onDeeplabMaskCreated(bitmap: Bitmap) {
            mDeeplabMaskObservers.forEach { it.onDeeplabMaskChanged(bitmap) }
        }

        override fun onRenderError(code: Int, msg: String) {

        }
    }

    override fun onStart() {
        mRenderService?.bindRenderContext(this)
        mImageSdk = Pixelator.create()
        mImageSdk.setRenderListener(mRenderListener)
        mContainer.getGestureService()?.addSingleMoveObserver(this)
        mContainer.getGestureService()?.addSingleUpObserver(this)
        mPaintSize = mContainer.getConfig().run { (minPaintSize + maxPaintSize) / 2 }
        setPaintSize(mPaintSize)
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
        runTaskOrPendding {
            val bitmap = BitmapFactory.decodeResource(mContainer.getContext().resources, id)
            mImageSdk.setBrush(bitmap)
        }
    }

    override fun setPaintSize(size: Int) {
        mPaintSize = size
        runTaskOrPendding {
            mImageSdk.setPaintSize(size)
        }
        mPaintSizeObservers.forEach { it.onPaintSizeChanged(size) }
    }

    override fun setPaintType(paintType: Int) {
        mPaintType = paintType
        mImageSdk.setPaintType(paintType)
    }

    override fun setDeeplabMask(bitmap: Bitmap) {
        mImageSdk.setDeeplabMask(bitmap)
    }

    override fun setDeeplabMode(mode: Int) {
        mImageSdk.setDeeplabMaskMode(mode)
    }

    override fun loadImage(path: String) {
        runTaskOrPendding {
            mImageSdk.addImagePath(path, getRotate(path))
            mImageSdk.refreshFrame()
            mImageLoadedObservers.forEach { it.onImageLoaded(path) }
        }
    }

    override fun setEffect(config: String) {
        runTaskOrPendding {
            mImageSdk.setEffect(config)
        }
    }

    override fun undo() {
        mImageSdk.undo()
    }

    override fun redo() {
        mImageSdk.redo()
    }

    override fun updateViewPort(offset: Int) {
        runTaskOrPendding {
            mImageSdk.updateViewPort(offset)
        }
    }

    override fun setTransformMatrix(matrix: Matrix) {
        mTransformMatrix.set(matrix)
        val v = FloatArray(9)
        mTransformMatrix.getValues(v)
        val glmArray = floatArrayOf(
                v[0], v[3], 0f, 0f,
                v[1], v[4], 0f, 0f,
                0f, 0f, 1f, 0f,
                v[2], v[5], 0f, 1f
        )
        mImageSdk.setMatrix(glmArray)
    }

    override fun getTransformMatrix(): Matrix {
        return mTransformMatrix
    }

    override fun getMiniScreen(): IMiniScreen {
        return mImageSdk.getMiniScreen()
    }

    override fun getContentBounds(): RectF {
        return mContentBounds
    }

    override fun getInitBounds(): RectF {
        return mInitBounds
    }

    override fun getPaintSize(): Int {
        return mPaintSize
    }

    override fun save() {
        mImageSdk.save()
    }

    override fun addPaintSizeObserver(observer: PaintSizeObserver) {
        if (!mPaintSizeObservers.contains(observer)) {
            mPaintSizeObservers.add(observer)
        }
    }

    override fun addDeeplabMaskObserver(observer: OnDeeplabMaskObserver) {
        if (!mDeeplabMaskObservers.contains(observer)) {
            mDeeplabMaskObservers.add(observer)
        }
    }

    override fun addContentBoundsObserver(observer: OnContentBoundsObserver) {
        if (!mContentBoundsObservers.contains(observer)) {
            mContentBoundsObservers.add(observer)
        }
    }

    override fun addImageLoadedObserver(observer: OnImageLoadedObserver) {
        if (!mImageLoadedObservers.contains(observer)) {
            mImageLoadedObservers.add(observer)
        }
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
        val points = PointUtils.pointsWith(from, to, control, mPaintSize.toFloat() / 5)
        val buffer = arrayListOf<Float>()
        points.forEach {
            buffer.add(it.x)
            buffer.add(it.y)
        }
        mImageSdk.pushTouchBuffer(buffer.toFloatArray(), current.x, current.y)
        mImageSdk.refreshFrame()
        return false
    }

    override fun onSingleUp(event: MotionEvent): Boolean {
        mImageSdk.stopTouch()
        return false
    }

    private fun runTaskOrPendding(task: Runnable) {
        if (mEglWindowCreated) {
            task.run()
        } else {
            mPenddingTasks.add(task)
        }
    }
}

interface IPixelatorCoreService : IService {
    fun setBrushResource(id: Int)
    fun setPaintSize(size: Int)
    fun setPaintType(@PaintType paintType: Int)
    fun setDeeplabMask(bitmap: Bitmap)
    fun setDeeplabMode(@MaskMode mode: Int)
    fun loadImage(path: String)
    fun setEffect(config: String)
    fun undo()
    fun redo()
    fun updateViewPort(offset: Int)
    fun setTransformMatrix(matrix: Matrix)
    fun getTransformMatrix(): Matrix
    fun getMiniScreen(): IMiniScreen
    fun getContentBounds(): RectF
    fun getInitBounds(): RectF
    fun getPaintSize(): Int
    fun save()
    fun addPaintSizeObserver(observer: PaintSizeObserver)
    fun addDeeplabMaskObserver(observer: OnDeeplabMaskObserver)
    fun addContentBoundsObserver(observer: OnContentBoundsObserver)
    fun addImageLoadedObserver(observer: OnImageLoadedObserver)
}

interface PaintSizeObserver {
    fun onPaintSizeChanged(size: Int)
}

interface OnDeeplabMaskObserver {
    fun onDeeplabMaskChanged(mask: Bitmap)
}

interface OnContentBoundsObserver {
    fun onContentBoundsChanged(bound: Rect)
}

interface OnImageLoadedObserver {
    fun onImageLoaded(path: String)
}