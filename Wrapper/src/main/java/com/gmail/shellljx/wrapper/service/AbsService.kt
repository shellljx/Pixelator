package com.gmail.shellljx.wrapper.service

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelStoreOwner
import com.gmail.shellljx.wrapper.ActivityViewModelStoreProvider
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.extension.safeAppCompatActivity

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/7
 * @Description:
 */
abstract class AbsService(protected val container: IContainer) : IService, LifecycleOwner, ViewModelStoreOwner, ActivityViewModelStoreProvider {

    override fun getLifecycle() = container.getServiceManager().lifecycle

    override fun getViewModelStore() = container.getServiceManager().viewModelStore

    override fun getActivityViewModelStoreOwner(): ViewModelStoreOwner {
        return checkNotNull(safeAppCompatActivity(container.getContext()))
    }
}