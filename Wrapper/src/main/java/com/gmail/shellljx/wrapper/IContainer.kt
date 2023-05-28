package com.gmail.shellljx.wrapper

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.gmail.shellljx.wrapper.layer.BuildInLayer
import com.gmail.shellljx.wrapper.layer.ILayer
import com.gmail.shellljx.wrapper.service.IDelegateService
import com.gmail.shellljx.wrapper.service.ILifecycleService
import com.gmail.shellljx.wrapper.service.control.IControlContainerService
import com.gmail.shellljx.wrapper.service.gesture.IGestureService
import com.gmail.shellljx.wrapper.service.panel.IPanelService
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService
import java.lang.IllegalArgumentException

interface IContainer {
    fun onCreate()
    fun onCreateView(container: ViewGroup?, savedInstanceState: Bundle?): View?
    fun onViewCreated(view: View, savedInstanceState: Bundle?)
    fun onStart()
    fun onResume()
    fun onPause()
    fun onStop()
    fun onDestroyView()
    fun onDestroy()
    fun onBackPressed(): Boolean
    fun dispatchWindowInsets(insets: Rect)
    fun updateViewPort(offset: Int)
    fun addCustomLayer(layer: ILayer, overBuildInLayer: BuildInLayer)
    fun removeCustomLayer(layer: ILayer)
    fun getServiceManager(): IServiceManager
    fun getLifeCycleService(): ILifecycleService?
    fun getRenderService(): IRenderContainerService?
    fun getControlService(): IControlContainerService?
    fun getPanelService(): IPanelService?
    fun getGestureService(): IGestureService?
    fun getDelegateService(): IDelegateService?
    fun getConfig(): Config
    fun getContext(): Context
    class Builder {
        private var mContext: Context? = null
        private var mConfig: Config? = null
        fun setContext(context: Context): Builder {
            mContext = context
            return this
        }

        fun setVEConfig(config: Config): Builder {
            mConfig = config
            return this
        }

        fun build(): IContainer {
            val context = mContext
            val config = mConfig
            if (context == null) {
                throw IllegalArgumentException("veContainer builder context is null")
            }
            if (config == null) {
                throw IllegalArgumentException("veContainer builder config is null")
            }
            return ContainerImpl(context, config)
        }
    }
}
