package com.gmail.shellljx.wrapper.service.control

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.widget.IWidget

class ControlContainerService : IControlContainerService, LifecycleObserver {
    private lateinit var mContainer: IContainer
    private var mControlContainer: ControlContainer? = null
    private var mWidgetMessageMap = hashMapOf<String, ArrayList<IWidget>>()
    private var isActive = false
    override fun onStart() {
        mContainer.getLifeCycleService()?.addObserver(this)
    }

    override fun bindVEContainer(veContainer: IContainer) {
        mContainer = veContainer
    }

    override fun createView(context: Context): IControlContainer {
        val controlContainer = ControlContainer(context)
        mControlContainer = controlContainer
        controlContainer.bindVEContainer(mContainer)
        return controlContainer
    }

    override fun registerWidgetMessage(widget: IWidget, vararg keys: String) {
        for (messageKey in keys) {
            var widgets = mWidgetMessageMap[messageKey]
            if (widgets == null) {
                widgets = arrayListOf()
                mWidgetMessageMap[messageKey] = widgets
            }
            widgets.add(widget)
        }
    }

    override fun unregisterWidgetMessage(widget: IWidget, vararg keys: String) {
        for (messageKey in keys) {
            val widgets = mWidgetMessageMap[messageKey] ?: return
            if (widgets.contains(widget)) {
                widgets.remove(widget)
            }
        }
    }

    override fun sendWidgetMessage(key: String, vararg args: Any) {
        val widgets = mWidgetMessageMap[key] ?: return
        widgets.forEach {
            it.onWidgetMessage(key, *args)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onActivityStart() {
        if (!isActive) {
            isActive = true
            mControlContainer?.switch()
        }
    }

    override fun show() {
        mControlContainer?.show()
    }

    override fun hide() {
        mControlContainer?.hide()
    }

    override fun isShowing(): Boolean {
        return mControlContainer?.isShowing() ?: false
    }

    override fun onStop() {
        mWidgetMessageMap.clear()
        mContainer.getLifeCycleService()?.removeObserver(this)
    }
}

interface IControlContainerService : IService {
    fun createView(context: Context): IControlContainer
    fun registerWidgetMessage(widget: IWidget, vararg keys: String)
    fun unregisterWidgetMessage(widget: IWidget, vararg keys: String)
    fun sendWidgetMessage(key: String, vararg args: Any)
    fun show()
    fun hide()
    fun isShowing(): Boolean
}
