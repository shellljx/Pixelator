package com.gmail.shellljx.wrapper

import androidx.lifecycle.ViewModelStoreOwner

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/10
 * @Description:
 */
interface ActivityViewModelStoreProvider {
    fun getActivityViewModelStoreOwner(): ViewModelStoreOwner
}