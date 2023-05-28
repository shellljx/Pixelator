package com.gmail.shellljx.wrapper.service.control

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.contains
import androidx.core.view.isVisible
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.Config
import com.gmail.shellljx.wrapper.widget.IWidget

class ControlContainer : FrameLayout, IControlContainer {
    private lateinit var mContainer: IContainer
    private var mControlConfig: Config.ControlContainerConfig? = null
    private val mWidgets = mutableListOf<IWidget>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    override fun bindVEContainer(veContainer: IContainer) {
        mContainer = veContainer
    }

    override fun setConfig(config: Config.ControlContainerConfig) {
        notifyWidgetsInactive()
        removeView(mControlConfig?.instance)
        mWidgets.clear()
        mControlConfig = config
        if (config.layoutRes == 0 && config.instance == null) {
            return
        }
        var targetView = config.instance
        if (targetView != null) {
            if (contains(targetView)) {
                return
            }
            addView(targetView)
            initWidgets(targetView)
        } else if (config.layoutRes > 0) {
            targetView = LayoutInflater.from(context).inflate(config.layoutRes, this, false)
            addView(targetView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            config.instance = targetView
            initWidgets(targetView)
        }
    }

    override fun switch() {
        bindContextToWidget()
        notifyWidgetsActive()
    }

    override fun show() {
        mControlConfig?.instance?.visibility = View.VISIBLE
    }

    override fun hide() {
        mControlConfig?.instance?.visibility = View.GONE
    }

    override fun isShowing(): Boolean {
        return mControlConfig?.instance?.isVisible ?: false
    }

    private fun initWidgets(view: View) {
        if (view !is ViewGroup) return
        if (view is IWidget) {
            mWidgets.add(view)
        }
        for (index in 0 until view.childCount) {
            val child = view.getChildAt(index)
            if (child is ViewGroup) {
                initWidgets(child)
            } else if (child is IWidget) {
                mWidgets.add(child)
            }
        }
    }

    private fun bindContextToWidget() {
        mWidgets.forEach {
            it.bindVEContainer(mContainer)
        }
    }

    private fun notifyWidgetsInactive() {
        mWidgets.forEach {
            it.onWidgetInactive()
        }
    }

    private fun notifyWidgetsActive() {
        mWidgets.forEach {
            it.onWidgetActive()
        }
    }

    override fun getView(): ViewGroup {
        return this
    }
}

interface IControlContainer {
    fun bindVEContainer(veContainer: IContainer)
    fun getView(): ViewGroup
    fun setConfig(config: Config.ControlContainerConfig)
    fun switch()
    fun show()
    fun hide()
    fun isShowing(): Boolean
}
