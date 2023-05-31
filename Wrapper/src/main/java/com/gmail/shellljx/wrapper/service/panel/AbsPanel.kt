package com.gmail.shellljx.wrapper.service.panel

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.annotation.LayoutRes
import androidx.lifecycle.*
import com.gmail.shellljx.wrapper.IContainer

@Keep
abstract class AbsPanel(private val context: Context) : IPanel, LifecycleOwner {
    private val mLifecycleRegistry by lazy { LifecycleRegistry(this) }
    lateinit var mToken: PanelToken
    private var mPanelView: View? = null
    private var mIsAttached = false
    private var mIsShowing = false
    open val panelConfig: PanelConfig
        get() {
            return PanelConfig()
        }

    override fun bindVEContainer(vecontainer: IContainer) {
        onBindVEContainer(vecontainer)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun updatePayload(any: Any) {
        onPayloadUpdate(any)
    }

    override fun getView(): View? {
        return mPanelView
    }

    override fun createView(container: ViewGroup): View? {
        if (mPanelView == null) {
            mPanelView = LayoutInflater.from(context).inflate(getLayoutId(), container, false)
            mPanelView?.isClickable = true
            onViewCreated(mPanelView)
        }
        return mPanelView
    }

    override fun attach() {
        mIsAttached = true
        mToken.isAttach = mIsAttached
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        onAttach()
    }

    override fun resume() {
        mIsShowing = true
        mToken.isTopStack = mIsShowing
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        onResume()
    }

    override fun pause() {
        mIsShowing = false
        mToken.isTopStack = mIsShowing
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        onPause()
    }

    override fun detach() {
        mIsAttached = false
        mToken.isAttach = mIsAttached
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        onDetach()
    }

    override fun isAttached(): Boolean {
        return mIsAttached
    }

    override fun isShowing(): Boolean {
        return mIsShowing
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    open fun onPayloadUpdate(any: Any) {}
    open fun onAttach() {}
    open fun onResume() {}
    open fun onPause() {}
    open fun onDetach() {}
    open fun onEnterAnimationStart() {}
    open fun onExitAnimationStart() {}
    abstract fun onBindVEContainer(vecontainer: IContainer)

    @LayoutRes
    abstract fun getLayoutId(): Int
    abstract fun onViewCreated(view: View?)
}