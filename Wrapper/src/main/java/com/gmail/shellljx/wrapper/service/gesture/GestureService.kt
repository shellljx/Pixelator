package com.gmail.shellljx.wrapper.service.gesture

import android.content.Context
import android.view.MotionEvent
import android.view.View
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.gesture.GesturePriorityProcessor.Companion.GESTURE_PRIORITY_NORMAL

class GestureService : IGestureService {
    private lateinit var mVEContainer: IContainer
    private var mGestureContaienr: GestureContaienr? = null
    private val mSingleTapProcessor = GesturePriorityProcessor<OnSingleTapListener>()
    private val mDoubleTapProcessor = GesturePriorityProcessor<OnDoubleTapListener>()
    private val mLongPressProcessor = GesturePriorityProcessor<OnLongPressListener>()
    private val mFlingProcessor = GesturePriorityProcessor<OnFlingListener>()
    private val mScrollProcessor = GesturePriorityProcessor<OnScrollListener>()
    private var mOnscaleListener: OnScaleListener? = null
    override fun onStart() {
    }

    override fun createView(context: Context): View? {
        if (mGestureContaienr == null) {
            mGestureContaienr = GestureContaienr(context)
        }
        mGestureContaienr?.setOnSingleTapListener(mSingleTapListener)
        mGestureContaienr?.setOnDoubleTapListener(mDoubleTapListener)
        mGestureContaienr?.setOnLongPressListener(mLongPressListener)
        mGestureContaienr?.setOnFlingListener(mFlingListener)
        mGestureContaienr?.setOnScrollListener(mOnScrollListener)
        mGestureContaienr?.setOnScaleListener(mOnscaleListener)
        return mGestureContaienr
    }

    private val mSingleTapListener = object : OnSingleTapListener {
        override fun onSingleTap(event: MotionEvent): Boolean {
            return mSingleTapProcessor.process {
                it.onSingleTap(event)
            }
        }
    }
    private val mDoubleTapListener = object : OnDoubleTapListener {
        override fun onDoubleTap(event: MotionEvent): Boolean {
            return mDoubleTapProcessor.process {
                it.onDoubleTap(event)
            }
        }
    }
    private val mLongPressListener = object : OnLongPressListener {
        override fun onLongPress(event: MotionEvent): Boolean {
            return mLongPressProcessor.process {
                it.onLongPress(event)
            }
        }
    }
    private val mFlingListener = object : OnFlingListener {
        override fun onHorizontalFling(direction: Direction, distance: Float): Boolean {
            return mFlingProcessor.process {
                it.onHorizontalFling(direction, distance)
            }
        }
    }

    private val mOnScrollListener = object : OnScrollListener {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            return mScrollProcessor.process {
                it.onScroll(e1, e2, distanceX, distanceY)
            }
        }
    }

    override fun addSingleTapListener(listener: OnSingleTapListener, priority: Int) {
        mSingleTapProcessor.add(listener, priority)
    }

    override fun removeSingleTapListener(listener: OnSingleTapListener) {
        mSingleTapProcessor.remove(listener)
    }

    override fun addDoubleTapListener(listener: OnDoubleTapListener, priority: Int) {
        mDoubleTapProcessor.add(listener, priority)
    }

    override fun removeDoubleTapListener(listener: OnDoubleTapListener) {
        mDoubleTapProcessor.remove(listener)
    }

    override fun addLongPressListener(listener: OnLongPressListener, priority: Int) {
        mLongPressProcessor.add(listener, priority)
    }

    override fun removeLongPressListener(listener: OnLongPressListener) {
        mLongPressProcessor.remove(listener)
    }

    override fun addScrollListener(listener: OnScrollListener, priority: Int) {
        mScrollProcessor.add(listener, priority)
    }

    override fun removeScrollListener(listener: OnScrollListener) {
        mScrollProcessor.remove(listener)
    }

    override fun addFlingListener(listener: OnFlingListener, priority: Int) {
        mFlingProcessor.add(listener, priority)
    }

    override fun removeFlingListener(listener: OnFlingListener) {
        mFlingProcessor.remove(listener)
    }

    override fun setOnScaleListener(listener: OnScaleListener?) {
        mOnscaleListener = listener
    }

    override fun bindVEContainer(veContainer: IContainer) {
        mVEContainer = veContainer
    }

    override fun onStop() {
    }
}

interface IGestureService : IService {
    fun createView(context: Context): View?
    fun addSingleTapListener(listener: OnSingleTapListener, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun removeSingleTapListener(listener: OnSingleTapListener)
    fun addDoubleTapListener(listener: OnDoubleTapListener, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun removeDoubleTapListener(listener: OnDoubleTapListener)
    fun addLongPressListener(listener: OnLongPressListener, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun removeLongPressListener(listener: OnLongPressListener)
    fun addScrollListener(listener: OnScrollListener, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun removeScrollListener(listener: OnScrollListener)
    fun addFlingListener(listener: OnFlingListener, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun removeFlingListener(listener: OnFlingListener)

    fun setOnScaleListener(listener: OnScaleListener?)
}

interface OnSingleTapListener {
    fun onSingleTap(event: MotionEvent): Boolean
}

interface OnDoubleTapListener {
    fun onDoubleTap(event: MotionEvent): Boolean
}

interface OnLongPressListener {
    fun onLongPress(event: MotionEvent): Boolean
}

interface OnScrollListener {
    fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean
}

interface OnFlingListener {
    /**
     * @param direction fling方向
     * @param distance 触发fling的滑动距离
     */
    fun onHorizontalFling(direction: Direction, distance: Float): Boolean
}

interface OnScaleListener {
    fun onScaleBegin(): Boolean
    fun onScale(scale: Float): Boolean
    fun onScaleEnd(): Boolean
}

enum class Direction {
    LEFT,
    RIGHT
}
