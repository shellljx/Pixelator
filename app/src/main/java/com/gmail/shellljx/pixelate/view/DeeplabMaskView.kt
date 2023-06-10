package com.gmail.shellljx.pixelate.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

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

    init {
        mMaskPaint.alpha = 50
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mMaskBitmap?.let {
            canvas.drawBitmap(it, mSrcBounds, mDstBounds, mMaskPaint)
        }
    }

    fun setMask(bounds: Rect, mask: Bitmap) {
        mMaskBitmap = mask
        mSrcBounds.set(0, 0, mask.width, mask.height)
        setContentBounds(bounds)
    }

    fun setContentBounds(dstBounds: Rect) {
        mDstBounds.set(dstBounds)
        invalidate()
    }
}