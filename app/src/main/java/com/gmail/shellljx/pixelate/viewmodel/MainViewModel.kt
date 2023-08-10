package com.gmail.shellljx.pixelate.viewmodel

import androidx.lifecycle.MutableLiveData

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/8
 * @Description:
 */
class MainViewModel : BaseViewModel() {
    val adStateLiveData = MutableLiveData<Boolean>()
    val saveImageLiveData = MutableLiveData<Int>()
    val savedImageLiveData = MutableLiveData<String>()
    val openAlbumLiveData = MutableLiveData<Int>()
}