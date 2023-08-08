package com.gmail.shellljx.pixelate

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
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
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this) {
            val adState = it.adapterStatusMap["com.google.android.gms.ads.MobileAds"]
            mainViewModel.adStateLiveData.postValue(adState?.initializationState == AdapterStatus.State.READY)
        }
        fragment = PixelatorFragment()
        supportFragmentManager.beginTransaction().replace(R.id.content, fragment!!).commit()
        setTransparent()
    }

    fun setTransparent() {
        transparentStatusBar(this)
        hintNavigationBar(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //fragment?.onActivityResult(requestCode, resultCode, data)
    }

    private fun transparentStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            //            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            activity.window.statusBarColor = Color.TRANSPARENT
        } else {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    fun hintNavigationBar(activity: Activity) {
        activity.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE) //API19
    }
}
