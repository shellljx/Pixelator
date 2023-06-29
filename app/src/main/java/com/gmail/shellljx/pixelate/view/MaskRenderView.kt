package com.gmail.shellljx.pixelate.view

import android.animation.*
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.view.isVisible
import com.gmail.shellljx.pixelator.MaskMode

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/8
 * @Description:
 */
class MaskRenderView @JvmOverloads constructor(
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
    private var mHideAnimator: ValueAnimator? = null

    init {
        mMaskPaint.alpha = 180
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
        invalidate()
        startHideAnimator()
    }

    fun setContentBounds(dstBounds: Rect) {
        mDstBounds.set(dstBounds)
        mXfermode?.let {
            invalidate()
        }
    }

    fun getMaskBitmap(): Bitmap? {
        return mMaskBitmap
    }

    fun isAnimating(): Boolean {
        return mHideAnimator?.isRunning ?: false
    }

    private fun startHideAnimator() {
        mHideAnimator?.cancel()
        mHideAnimator?.removeAllUpdateListeners()
        mHideAnimator?.removeAllListeners()
        if (mHideAnimator == null) {
            mHideAnimator = ValueAnimator.ofFloat(1f, 0f)
            mHideAnimator?.duration = 300
            mHideAnimator?.startDelay = 500
        }
        mHideAnimator?.addUpdateListener {
            val value = it.animatedValue as Float
            alpha = value
        }
        mHideAnimator?.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isVisible = false
                alpha = 1f
            }
        })
        isVisible = true
        mHideAnimator?.start()
    }
}