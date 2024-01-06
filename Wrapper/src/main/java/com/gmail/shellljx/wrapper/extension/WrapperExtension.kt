package com.gmail.shellljx.wrapper.extension

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.gmail.shellljx.wrapper.ActivityViewModelStoreProvider
import kotlin.reflect.KClass

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/10
 * @Description:
 */

@MainThread
inline fun <reified T, reified VM : ViewModel> T.activityViewModels(
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) where T : ActivityViewModelStoreProvider = createViewModelLazy(VM::class, { getActivityViewModelStoreOwner().viewModelStore }, factoryProducer)

@MainThread
inline fun <reified VM : ViewModel> Fragment.activityViewModels(
    noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createViewModelLazy(VM::class, { requireActivity().viewModelStore }, factoryProducer)


@MainThread
inline fun <reified VM : ViewModel> View.activityViewModels(
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) = createViewModelLazy(VM::class, { checkNotNull(safeAppCompatActivity(context)).viewModelStore }, factoryProducer)

@MainThread
inline fun <T, reified VM : ViewModel> T.viewModels(
        noinline ownerProducer: () -> ViewModelStoreOwner = { this },
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null
) where T : ViewModelStoreOwner, T : LifecycleOwner = createViewModelLazy(VM::class, { ownerProducer().viewModelStore }, factoryProducer)

@MainThread
fun <VM : ViewModel> createViewModelLazy(
        viewModelClass: KClass<VM>,
        owner: ViewModelStoreOwner,
        factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: { getDefaultViewModelProviderFactory(owner) }
    return ViewModelLazy(viewModelClass, { owner.viewModelStore }, factoryPromise)
}

@MainThread
fun getDefaultViewModelProviderFactory(owner: ViewModelStoreOwner): ViewModelProvider.Factory =
        if (owner is HasDefaultViewModelProviderFactory) owner.defaultViewModelProviderFactory else ViewModelProvider.NewInstanceFactory()

fun safeAppCompatActivity(context: Context): AppCompatActivity? {
    if (context is AppCompatActivity) {
        return context
    } else {
        var safeContext = context
        while (safeContext is ContextWrapper) {
            if (safeContext is AppCompatActivity) {
                return safeContext
            }
            safeContext = safeContext.baseContext
        }
    }
    return null
}