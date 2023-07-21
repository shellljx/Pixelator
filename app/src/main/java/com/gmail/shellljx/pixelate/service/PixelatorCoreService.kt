package com.gmail.shellljx.pixelate.service

import android.graphics.*
import android.media.ExifInterface
import android.view.MotionEvent
import android.view.Surface
import androidx.core.graphics.toRect
import com.gmail.shellljx.pixelator.*
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.*
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService
import com.gmail.shellljx.wrapper.service.render.RenderContainerService
import com.gmail.shellljx.wrapper.utils.PointUtils
import java.util.LinkedList

class PixelatorCoreService : IPixelatorCoreService, IRenderContext, OnSingleMoveObserver, OnTapObserver {
    private lateinit var mContainer: IContainer
    private var mRenderService: IRenderContainerService? = null

    private lateinit var mImageSdk: IPixelator
    private val mPenddingTasks = LinkedList<Runnable>()
    private val mInitBounds = RectF() //图片插入成功初始 bounds
    private val mContentBounds = RectF() //图片变换后最新的bounds
    private val mTransformMatrix = Matrix() //变换矩阵
    private var mPaintSize = 0
    private var mImagePath: String? = null

    @PaintMode
    private var mPaintMode = PAINT
    @PaintType
    private var mPaintType = Graffiti
    private var mEglWindowCreated = false
    private val mPaintSizeObservers = arrayListOf<PaintSizeObserver>()
    private val mContentBoundsObservers = arrayListOf<OnContentBoundsObserver>()
    private val mImageLoadedObservers = arrayListOf<OnImageLoadedObserver>()
    private val mUndoRedoStateObservers = arrayListOf<UndoRedoStateObserver>()

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

        override fun onFrameBoundsChanged(left: Float, top: Float, right: Float, bottom: Float, reset: Boolean) {
            if (reset) {
                mInitBounds.set(left, top, right, bottom)
                mTransformMatrix.reset()
            }
            mContentBounds.set(left, top, right, bottom)
            mContentBoundsObservers.forEach { it.onContentBoundsChanged(mContentBounds.toRect()) }
        }

        override fun onInitBoundsChanged(left: Float, top: Float, right: Float, bottom: Float) {
            mInitBounds.set(left, top, right, bottom)
        }

        override fun onFrameSaved(bitmap: Bitmap) {
        }

        override fun onRenderError(code: Int, msg: String) {

        }

        override fun onUndoRedoChanged(canUndo: Boolean, canRedo: Boolean) {
            mUndoRedoStateObservers.forEach { it.onUndoRedoStateChange(canUndo, canRedo) }
        }
    }

    override fun onStart() {
        mRenderService?.bindRenderContext(this)
        mImageSdk = Pixelator.create()
        mImageSdk.setRenderListener(mRenderListener)
        mContainer.getGestureService()?.addSingleMoveObserver(this)
        mContainer.getGestureService()?.addTapObserver(this)
        mPaintSize = mContainer.getConfig().run { (minPaintSize + maxPaintSize) / 2 }
        setPaintSize(mPaintSize)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
        mRenderService = mContainer.getServiceManager().getService(RenderContainerService::class.java)
    }

    override fun onStop() {
        mImageSdk.destroy()
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

    override fun setPaintMode(paintMode: Int) {
        mPaintMode = paintMode
        mImageSdk.setPaintMode(paintMode)
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
            mImagePath = path
            mImageLoadedObservers.forEach { it.onImageLoaded(path) }
        }
    }

    override fun refreshFrame() {
        runTaskOrPendding {
            mImageSdk.refreshFrame()
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

    override fun getPaintType(): Int {
        return mPaintType
    }

    override fun getPaintMode(): Int {
        return mPaintMode
    }

    override fun getImagePath(): String? {
        return mImagePath
    }

    override fun save() {
        mImageSdk.save()
    }

    override fun addPaintSizeObserver(observer: PaintSizeObserver) {
        if (!mPaintSizeObservers.contains(observer)) {
            mPaintSizeObservers.add(observer)
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

    override fun addUndoRedoStateObserver(observer: UndoRedoStateObserver) {
        if (!mUndoRedoStateObservers.contains(observer)) {
            mUndoRedoStateObservers.add(observer)
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

    override fun onSingleDown(event: MotionEvent): Boolean {
        mImageSdk.startTouch(event.x, event.y)
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
    fun setPaintMode(@PaintMode paintMode: Int)
    fun setPaintType(@PaintType paintType: Int)
    fun setDeeplabMask(bitmap: Bitmap)
    fun setDeeplabMode(@MaskMode mode: Int)
    fun loadImage(path: String)
    fun refreshFrame()
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
    @PaintType
    fun getPaintType(): Int
    @PaintMode
    fun getPaintMode(): Int
    fun getImagePath(): String?
    fun save()
    fun addPaintSizeObserver(observer: PaintSizeObserver)
    fun addContentBoundsObserver(observer: OnContentBoundsObserver)
    fun addImageLoadedObserver(observer: OnImageLoadedObserver)
    fun addUndoRedoStateObserver(observer: UndoRedoStateObserver)
}

interface PaintSizeObserver {
    fun onPaintSizeChanged(size: Int)
}

interface OnContentBoundsObserver {
    fun onContentBoundsChanged(bound: Rect)
}

interface OnImageLoadedObserver {
    fun onImageLoaded(path: String)
}

interface UndoRedoStateObserver {
    fun onUndoRedoStateChange(canUndo: Boolean, canRedo: Boolean)
}