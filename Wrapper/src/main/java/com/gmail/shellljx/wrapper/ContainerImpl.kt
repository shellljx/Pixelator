package com.gmail.shellljx.wrapper

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import com.gmail.shellljx.wrapper.layer.*
import com.gmail.shellljx.wrapper.service.IDelegateService
import com.gmail.shellljx.wrapper.service.ILifecycleService
import com.gmail.shellljx.wrapper.service.control.IControlContainerService
import com.gmail.shellljx.wrapper.service.gesture.IGestureService
import com.gmail.shellljx.wrapper.service.panel.IPanelService
import com.gmail.shellljx.wrapper.service.render.IRenderContainerService

class ContainerImpl internal constructor(
    private val context: Context,
    private val config: Config
) : IContainer {
    private lateinit var mVEServiceManager: ServiceManagerImpl
    private var mLifecycleService: ILifecycleService? = null
    private var mRenderContainerService: IRenderContainerService? = null
    private var mControlContainerService: IControlContainerService? = null
    private var mPanelService: IPanelService? = null
    private var mGestureService: IGestureService? = null
    private var mDelegateService: IDelegateService? = null
    private var mLayerContainer: ILayerContainer? = null
    override fun onCreate() {
        //加载必需的核心service
        initCoreServices()
        mLifecycleService?.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
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
        mLifecycleService?.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onResume() {
        mLifecycleService?.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onPause() {
        mLifecycleService?.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop() {
        mLifecycleService?.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    private fun initCoreServices() {
        mVEServiceManager = ServiceManagerImpl(this)
        CoreServicesConfig.CoreServices.forEach {
            mVEServiceManager.startService(it)
        }
        getLifeCycleService()
        getRenderService()
        getControlService()
        getPanelService()
        getGestureService()
        getDelegateService()
    }

    override fun getServiceManager(): IServiceManager {
        return mVEServiceManager
    }

    override fun getLifeCycleService(): ILifecycleService? {
        if (mLifecycleService == null) {
            mLifecycleService = mVEServiceManager.getService(CoreServicesConfig.LifeCycleService)
        }
        return mLifecycleService
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

    override fun getDelegateService(): IDelegateService? {
        if (mDelegateService == null) {
            mDelegateService = mVEServiceManager.getService(CoreServicesConfig.DelegateService)
        }
        return mDelegateService
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
        mLifecycleService?.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        mVEServiceManager.destroy()
    }

    override fun onBackPressed(): Boolean {
        return getPanelService()?.onBackPressed() ?: false
    }

    override fun dispatchWindowInsets(insets: Rect) {
        mLayerContainer?.dispatchWindowInsets(insets)
    }
}
