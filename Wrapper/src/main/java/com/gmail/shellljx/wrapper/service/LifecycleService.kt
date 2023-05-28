package com.gmail.shellljx.wrapper.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService

class LifecycleService : ILifecycleService, LifecycleOwner {
    private val mLifecycleRegistry = LifecycleRegistry(this)

    override fun onStart() {

    }

    override fun handleLifecycleEvent(event: Lifecycle.Event) {
        mLifecycleRegistry.handleLifecycleEvent(event)
    }

    override fun bindVEContainer(veContainer: IContainer) {
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
}

interface ILifecycleService : IService {
    fun handleLifecycleEvent(event: Lifecycle.Event)
    fun addObserver(listener: LifecycleObserver)
    fun removeObserver(listener: LifecycleObserver)
}
