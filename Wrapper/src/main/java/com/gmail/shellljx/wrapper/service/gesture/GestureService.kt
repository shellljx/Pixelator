package com.gmail.shellljx.wrapper.service.gesture

import android.content.Context
import android.graphics.Matrix
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
    private val mSingleUpProcessor = GesturePriorityProcessor<OnSingleUpObserver>()
    private val mSingleTapProcessor = GesturePriorityProcessor<OnSingleTapObserver>()
    private val mSingleDownProcessor = GesturePriorityProcessor<OnSingleDownObserver>()

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
        mSingleUpProcessor.clear()
        mSingleDownProcessor.clear()
    }

    override fun onSingleDown(event: MotionEvent) {
        mSingleDownProcessor.process {
            it.onSingleDown(event)
        }
    }

    override fun onSingleUp(event: MotionEvent) {
        mSingleUpProcessor.process {
            it.onSingleUp(event)
        }
    }

    override fun onSingleTap() {
        mSingleTapProcessor.process {
            it.onSingleTap()
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

    override fun addSingleUpObserver(observer: OnSingleUpObserver, priority: Int) {
        mSingleUpProcessor.add(observer, priority)
    }

    override fun addSingleTapObserver(observer: OnSingleTapObserver, priority: Int) {
        mSingleTapProcessor.add(observer, priority)
    }

    override fun addTransformObserver(observer: OnTransformObserver, priority: Int) {
        mTransformProcessor.add(observer, priority)
    }

    override fun addSingleDownObserver(observer: OnSingleDownObserver, priority: Int) {
        mSingleDownProcessor.add(observer, priority)
    }
}

interface IGestureService : IService {
    fun createView(context: Context): View?
    fun gestureEnable(enable: Boolean)
    fun addSingleMoveObserver(observer: OnSingleMoveObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun addSingleUpObserver(observer: OnSingleUpObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun addSingleTapObserver(observer: OnSingleTapObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun addTransformObserver(observer: OnTransformObserver, priority: Int = GESTURE_PRIORITY_NORMAL)
    fun addSingleDownObserver(observer: OnSingleDownObserver, priority: Int = GESTURE_PRIORITY_NORMAL)

}

interface OnSingleDownObserver {
    fun onSingleDown(event: MotionEvent): Boolean
}

interface OnSingleUpObserver {
    fun onSingleUp(event: MotionEvent): Boolean
}

interface OnSingleTapObserver {
    fun onSingleTap(): Boolean
}

interface OnSingleMoveObserver {
    fun onSingleMove(from: PointF, to: PointF, control: PointF, current: PointF): Boolean
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