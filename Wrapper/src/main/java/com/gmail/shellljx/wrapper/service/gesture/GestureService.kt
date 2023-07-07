package com.gmail.shellljx.wrapper.service.gesture

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.service.gesture.GesturePriorityProcessor.Companion.GESTURE_PRIORITY_NORMAL

class GestureService : IGestureService, GestureContaienr.GestureListener {
    private lateinit var mContainer: IContainer
    private var mGestureContaienr: GestureContaienr? = null
    private val mSingleMoveProcessor = GesturePriorityProcessor<OnSingleMoveObserver>()
    private val mTransformProcessor = GesturePriorityProcessor<OnTransformObserver>()
    private val mTapProcessor = GesturePriorityProcessor<OnTapObserver>()

    override fun onStart() {
    }

    override fun createView(context: Context): View? {
        if (mGestureContaienr == null) {
            mGestureContaienr = GestureContaienr(context)
            mGestureContaienr?.setGestureListener(this)
        }
        return mGestureContaienr
    }

    override fun gestureEnable(enable: Boolean) {
        mGestureContaienr?.setGestureEnable(enable)
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun onStop() {
        mSingleMoveProcessor.clear()
        mTransformProcessor.clear()
    }

    override fun onSingleDown(event: MotionEvent) {
        mTapProcessor.process {
            it.onSingleDown(event)
        }
    }

    override fun onSingleUp(event: MotionEvent) {
        mTapProcessor.process {
            it.onSingleUp(event)
        }
    }

    override fun onSingleTap() {
        mTapProcessor.process {
            it.onSingleTap()
        }
    }

    override fun onDoubleTap() {
        mTapProcessor.process {
            it.onDoubleTap()
        }
    }

    override fun onStartSingleMove() {
        mSingleMoveProcessor.process {
            it.onStartSingleMove()
        }
    }

    override fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF) {
        mSingleMoveProcessor.process {
            it.onSingleMove(from, to, control, current)
        }
    }

    override fun onTransformStart(point: PointF, point2: PointF) {
        mTransformProcessor.process {
            it.onTransformStart(point, point2)
        }
    }

    override fun onTransform(lastPoint: PointF, lastPoint2: PointF, point: PointF, point2: PointF) {
        mTransformProcessor.process {
            it.onTransform(lastPoint, lastPoint2, point, point2)
        }
    }

    override fun onTransformEnd() {
        mTransformProcessor.process {
            it.onTransformEnd()
        }
    }

    override fun addSingleMoveObserver(observer: OnSingleMoveObserver, priority: Int) {
        mSingleMoveProcessor.add(observer, priority)
    }

    override fun addTapObserver(observer: OnTapObserver, priority: Int) {
        mTapProcessor.add(observer, priority)
    }

    override fun addTransformObserver(observer: OnTransformObserver, priority: Int) {
        mTransformProcessor.add(observer, priority)
    }
}

interface IGestureService : IService {
    fun createView(context: Context): View?
    fun gestureEnable(enable: Boolean)
    fun addSingleMoveObserver(observer: OnSingleMoveObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun addTapObserver(observer: OnTapObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun addTransformObserver(observer: OnTransformObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
}

interface OnTapObserver {
    fun onSingleUp(event: MotionEvent): Boolean {
        return false
    }

    fun onSingleDown(event: MotionEvent): Boolean {
        return false
    }

    fun onSingleTap(): Boolean {
        return false
    }

    fun onDoubleTap(): Boolean {
        return false
    }
}

interface OnSingleMoveObserver {
    fun onStartSingleMove(): Boolean {
        return false
    }

    fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF): Boolean {
        return false
    }
}

interface OnTransformObserver {
    fun onTransformStart(point: PointF, point2: PointF): Boolean {
        return false
    }

    fun onTransform(lastPoint: PointF, lastPoint2: PointF, point: PointF, point2: PointF): Boolean
    fun onTransformEnd(): Boolean {
        return false
    }
}