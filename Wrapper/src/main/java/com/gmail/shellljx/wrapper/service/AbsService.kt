package com.gmail.shellljx.wrapper.service

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/7
 * @Description:
 */
abstract class AbsService : IService, LifecycleOwner, ViewModelStoreOwner {
    private lateinit var mVEContainer: IContainer

    override fun bindVEContainer(container: IContainer) {
        mVEContainer = container
    }

    override fun getLifecycle() = mVEContainer.getServiceManager().lifecycle

    override fun getViewModelStore() = mVEContainer.getServiceManager().viewModelStore
}