package com.gmail.shellljx.wrapper

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.gmail.shellljx.wrapper.layer.*
import com.gmail.shellljx.wrapper.service.control.IControlContainerService
import com.gmail.shellljx.wrapper.service.gesture.IGestureService
import com.gmail.shellljx.wrapper.service.panel.IPanelService
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService

class ContainerImpl internal constructor(
    private val context: Context,
    private val config: Config
) : IContainer {
    private lateinit var mVEServiceManager: ServiceManagerImpl
    private var mRenderContainerService: IRenderContainerService? = null
    private var mControlContainerService: IControlContainerService? = null
    private var mPanelService: IPanelService? = null
    private var mGestureService: IGestureService? = null
    private var mLayerContainer: ILayerContainer? = null
    override fun onCreate() {
        //加载必需的核心service
        initCoreServices()
        mVEServiceManager.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateView(container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (mLayerContainer == null) {
            mLayerContainer = VELayerContainer(getContext())
            mLayerContainer?.init(this)
        }
        return mLayerContainer?.getView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onStart() {
        mVEServiceManager.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        mVEServiceManager.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        mVEServiceManager.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop() {
        mVEServiceManager.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private fun initCoreServices() {
        mVEServiceManager = ServiceManagerImpl(this)
        CoreServicesConfig.CoreServices.forEach {
            mVEServiceManager.startService(it)
        }
        getRenderService()
        getControlService()
        getPanelService()
        getGestureService()
    }

    override fun getServiceManager(): IServiceManager {
        return mVEServiceManager
    }

    override fun getRenderService(): IRenderContainerService? {
        if (mRenderContainerService == null) {
            mRenderContainerService = mVEServiceManager.getService(CoreServicesConfig.RenderContainerService)
        }
        return mRenderContainerService
    }

    override fun getControlService(): IControlContainerService? {
        if (mControlContainerService == null) {
            mControlContainerService = mVEServiceManager.getService(CoreServicesConfig.ControlContainerService)
        }
        return mControlContainerService
    }

    override fun getPanelService(): IPanelService? {
        if (mPanelService == null) {
            mPanelService = mVEServiceManager.getService(CoreServicesConfig.PanelService)
        }
        return mPanelService
    }

    override fun getGestureService(): IGestureService? {
        if (mGestureService == null) {
            mGestureService = mVEServiceManager.getService(CoreServicesConfig.GestureService)
        }
        return mGestureService
    }

    override fun getConfig(): Config {
        return config
    }

    override fun getContext(): Context {
        return context
    }

    override fun updateViewPort(offset: Int) {
        mLayerContainer?.updateViewPort(offset)
    }

    override fun onDestroyView() {
    }

    override fun onDestroy() {
        mVEServiceManager.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        mVEServiceManager.destroy()
    }

    override fun onBackPressed(): Boolean {
        return getPanelService()?.onBackPressed() ?: false
    }

    override fun dispatchWindowInsets(insets: Rect) {
        mLayerContainer?.dispatchWindowInsets(insets)
    }
}
