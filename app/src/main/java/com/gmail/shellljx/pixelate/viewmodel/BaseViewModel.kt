package com.gmail.shellljx.pixelate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/7
 * @Description:
 */
open class BaseViewModel : ViewModel(), CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, exception: Throwable) {
    }

    fun launchSafely(
        context: CoroutineContext = Dispatchers.Main.immediate,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(context + this, start, block)
    }
}