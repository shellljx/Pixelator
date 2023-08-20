package com.gmail.shellljx.pixelate

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.gmail.shellljx.pixelate.viewmodel.MainViewModel
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus

class MainActivity : AppCompatActivity() {
    private var fragment: PixelatorFragment? = null
    private val mainViewModel by lazy { ViewModelProvider(this, defaultViewModelProviderFactory)[MainViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.statusBarColor = resources.getColor(android.R.color.black)
        }
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {
            val adState = it.adapterStatusMap["com.google.android.gms.ads.MobileAds"]
            mainViewModel.adStateLiveData.postValue(adState?.initializationState == AdapterStatus.State.READY)
        }
        fragment = PixelatorFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment!!).commit()
    }
}
