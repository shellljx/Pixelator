package com.gmail.shellljx.wrapper

import com.gmail.shellljx.wrapper.service.render.RenderContainerService
import com.gmail.shellljx.wrapper.service.control.ControlContainerService
import com.gmail.shellljx.wrapper.service.gesture.GestureService
import com.gmail.shellljx.wrapper.service.LifecycleService
import com.gmail.shellljx.wrapper.service.panel.PanelService
import com.gmail.shellljx.wrapper.service.DelegateService
import java.lang.Exception
import java.util.logging.Logger

class ServiceManagerImpl(private val veContainer: IContainer) : IServiceManager {
    companion object {
        private const val TAG = "ServiceManagerImpl"
    }

    private val mServiceRecords = HashMap<String, VEServiceRecord>()
    private val mBusinessServices = arrayListOf<Class<out IService>>()
    override fun registerBusinessService(businessServices: List<Class<out IService>>) {
        mBusinessServices.addAll(businessServices.filter { !mBusinessServices.contains(it) })
        mBusinessServices.forEach {
            startService(it)
        }
    }

    override fun <T : IService> startService(clazz: Class<T>) {
        val descriptor = clazz.simpleName
        var record = mServiceRecords[descriptor]
        if (record != null) {
            return
        }
        if (!CoreServicesConfig.CoreServices.contains(clazz) && !mBusinessServices.contains(clazz)) {
            return
        }
        val service = createService(clazz)
        record = VEServiceRecord(clazz)
        record.instance = service
        mServiceRecords[descriptor] = record
        record.instance?.onStart()
    }

    private fun <T : IService> createService(clazz: Class<T>): T? {
        return try {
            val constructor = clazz.getConstructor()
            val instance = constructor.newInstance()
            instance.bindVEContainer(veContainer)
            instance
        } catch (e: Exception) {
            null
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : IService> getService(clazz: Class<T>): T? {
        val descriptor = clazz.simpleName
        Logger.getLogger(TAG).info("get service: $descriptor")
        var record: VEServiceRecord? = null
        if (mServiceRecords.containsKey(descriptor)) {
            record = findServiceRecord(clazz)
        } else {
            startService(clazz)
        }
        if (record == null) {
            record = findServiceRecord(clazz)
        }
        return record?.instance as? T?
    }

    override fun <T : IService> stopService(clazz: Class<T>) {
        val descriptor = clazz.simpleName
        Logger.getLogger(TAG).info("stop service $descriptor")
        val record = findServiceRecord(clazz)
        if (record == null) {
            Logger.getLogger(TAG).info("service $descriptor do not started!!")
            return
        }
        record.instance?.onStop()
    }

    override fun destroy() {
        val iterator = mServiceRecords.values.iterator()
        while (iterator.hasNext()) {
            val record = iterator.next()
            stopService(record.clazz)
            iterator.remove()
        }
    }

    private fun <T : IService> findServiceRecord(clazz: Class<T>): VEServiceRecord? {
        return mServiceRecords[clazz.simpleName]
    }

    private class VEServiceRecord(val clazz: Class<out IService>) {
        var instance: IService? = null
    }
}

internal object CoreServicesConfig {
    internal val LifeCycleService = LifecycleService::class.java
    internal val RenderContainerService = RenderContainerService::class.java
    internal val ControlContainerService = ControlContainerService::class.java
    internal val PanelService = PanelService::class.java
    internal val GestureService = GestureService::class.java
    internal val DelegateService = DelegateService::class.java
    internal val CoreServices = mutableListOf(
        LifeCycleService,
        GestureService,
        RenderContainerService,
        ControlContainerService,
        PanelService,
        DelegateService
    )
}
