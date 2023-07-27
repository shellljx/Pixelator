package com.gmail.shellljx.wrapper.service

import androidx.annotation.Keep
import androidx.lifecycle.*
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import kotlinx.coroutines.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.coroutines.CoroutineContext
@Keep
class LifecycleService : ILifecycleService, LifecycleOwner, CoroutineExceptionHandler {
    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        Logger.getLogger("LifecycleService").log(Level.WARNING, exception.message)
    }

    private val mLifecycleRegistry = LifecycleRegistry(this)

    override fun onStart() {

    }

    override fun handleLifecycleEvent(event: Lifecycle.Event) {
        mLifecycleRegistry.handleLifecycleEvent(event)
    }

    override fun bindVEContainer(container: IContainer) {
    }

    override fun onStop() {
    }

    override fun addObserver(listener: LifecycleObserver) {
        mLifecycleRegistry.addObserver(listener)
    }

    override fun removeObserver(listener: LifecycleObserver) {
        mLifecycleRegistry.removeObserver(listener)
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    override fun launchSafely(context: CoroutineContext, start: CoroutineStart, block: suspend CoroutineScope.() -> Unit) {
        lifecycle.coroutineScope.launch(context + this, start, block)
    }
}

interface ILifecycleService : IService {
    fun handleLifecycleEvent(event: Lifecycle.Event)
    fun addObserver(listener: LifecycleObserver)
    fun removeObserver(listener: LifecycleObserver)

    fun launchSafely(
        context: CoroutineContext = Dispatchers.Main.immediate,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    )
}
