package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.gmail.shellljx.pixelator.MaskMode

/**
 * @Author: lijinxiang
 * @Email: lijinxiang@shizhuang-inc.com
 * @Date: 2023/6/8
 * @Description:
 */
class DeeplabMaskView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var mMaskBitmap: Bitmap? = null
    private val mMaskPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mSrcBounds = Rect()
    private val mDstBounds = Rect()
    private var mOverlayBitmap: Bitmap? = null
    private var mXfermode: PorterDuffXfermode? = null

    init {
        mMaskPaint.alpha = 200
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mXfermode == null) {
            return
        }
        mMaskBitmap?.let {
            canvas.drawBitmap(it, mSrcBounds, mDstBounds, mMaskPaint)
        }
        mMaskPaint.xfermode = mXfermode
        mOverlayBitmap?.let {
            canvas.drawBitmap(it, mSrcBounds, mDstBounds, mMaskPaint)
        }
        mMaskPaint.xfermode = null
    }

    fun setMask(bounds: Rect, mask: Bitmap) {
        mMaskBitmap = mask
        mSrcBounds.set(0, 0, mask.width, mask.height)
        mOverlayBitmap?.recycle()
        mOverlayBitmap = Bitmap.createBitmap(mask.width, mask.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mOverlayBitmap!!)
        canvas.drawColor(Color.RED)
        setContentBounds(bounds)
    }

    fun setMaskMode(@MaskMode mode: Int) {
        mXfermode = when (mode) {
            MaskMode.PERSON -> {
                PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            }

            MaskMode.BACKGROUND -> {
                PorterDuffXfermode(PorterDuff.Mode.SRC_OUT)
            }

            else -> {
                null
            }
        }
    }

    fun setContentBounds(dstBounds: Rect) {
        mDstBounds.set(dstBounds)
        mXfermode?.let {
            invalidate()
        }
    }
}