package com.gmail.shellljx.wrapper.service

import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService

class DelegateService : IDelegateService {
    private val mDelegateMap = hashMapOf<String, AbsDelegate>()
    override fun onStart() {
    }

    override fun bindVEContainer(veContainer: IContainer) {
    }

    override fun putDelegate(key: String, delegate: AbsDelegate) {
        mDelegateMap[key] = delegate
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : AbsDelegate> getDelegate(key: String): T? {
        return mDelegateMap[key] as? T
    }

    override fun onStop() {
    }
}

interface IDelegateService : IService {
    fun putDelegate(key: String, delegate: AbsDelegate)
    fun <T : AbsDelegate> getDelegate(key: String): T?
}

interface AbsDelegate
