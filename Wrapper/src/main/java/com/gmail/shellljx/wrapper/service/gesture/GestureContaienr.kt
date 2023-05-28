package com.gmail.shellljx.wrapper.service.gesture

import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import kotlin.math.abs

class GestureContaienr : FrameLayout {
    private val mGestureDetector: GestureDetector
    private val mScaleGestureDetector: ScaleGestureDetector
    private var mOnSingleTapListener: OnSingleTapListener? = null
    private var mOnDoubleTapListener: OnDoubleTapListener? = null
    private var mOnLongPressListener: OnLongPressListener? = null
    private var mOnScrollListener: OnScrollListener? = null
    private var mOnFlingListener: OnFlingListener? = null
    private var mOnScaleListener: OnScaleListener? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def) {
        mGestureDetector = GestureDetector(context, mOnGestureListener)
        mScaleGestureDetector = ScaleGestureDetector(context, mOnScrollGestureListener)
    }

    private val mOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            parent.requestDisallowInterceptTouchEvent(true)
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return mOnSingleTapListener?.onSingleTap(e) ?: false
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return mOnDoubleTapListener?.onDoubleTap(e) ?: false
        }

        override fun onLongPress(e: MotionEvent) {
            mOnLongPressListener?.onLongPress(e)
        }

        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return mOnScrollListener?.onScroll(e1, e2, distanceX, distanceY) ?: false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (e1 == null || e2 == null) {
                return false
            }
            val direction = if (e2.x > e1.x) Direction.RIGHT else Direction.LEFT
            return mOnFlingListener?.onHorizontalFling(direction, abs(e2.x - e1.x)) ?: false
        }
    }

    private val mOnScrollGestureListener = object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            return mOnScaleListener?.onScale(detector.scaleFactor) ?: false
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return mOnScaleListener?.onScaleBegin() ?: false
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            mOnScaleListener?.onScaleEnd()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        var consumed = mScaleGestureDetector.onTouchEvent(event)
        if (!mScaleGestureDetector.isInProgress) {
            consumed = consumed or mGestureDetector.onTouchEvent(event)

        }
        return consumed
    }

    fun setOnSingleTapListener(singleTapListener: OnSingleTapListener?) {
        mOnSingleTapListener = singleTapListener
    }

    fun setOnDoubleTapListener(doubleTapListener: OnDoubleTapListener?) {
        mOnDoubleTapListener = doubleTapListener
    }

    fun setOnLongPressListener(longPressListener: OnLongPressListener?) {
        mOnLongPressListener = longPressListener
    }

    fun setOnScrollListener(scrollListener: OnScrollListener?) {
        mOnScrollListener = scrollListener
    }

    fun setOnFlingListener(flingListener: OnFlingListener?) {
        mOnFlingListener = flingListener
    }

    fun setOnScaleListener(scaleListener: OnScaleListener?) {
        mOnScaleListener = scaleListener
    }
}
