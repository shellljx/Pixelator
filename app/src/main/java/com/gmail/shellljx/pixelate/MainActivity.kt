package com.gmail.shellljx.pixelate

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gmail.shellljx.pixelate.utils.PermissionUtils
import com.gmail.shellljx.pixelate.viewmodel.MediaViewModel
import com.gmail.shellljx.wrapper.extension.viewModels

class MainActivity : AppCompatActivity() {
    private val imageEditFragment: ImageEditFragment by lazy { ImageEditFragment() }
    private val mediaFragment: MediaFragment by lazy { MediaFragment() }
    private val mediaViewModel: MediaViewModel by viewModels()
    private val mediaPermission by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
    private lateinit var permissionLayout: View
    private lateinit var openPermissionBtn: View
    private var mediaPermissionGranted = false

    private val readFilePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showMediaFragment()
        } else {
            Toast.makeText(this, getString(R.string.request_permission_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.statusBarColor = resources.getColor(android.R.color.black)
        }
        setContentView(R.layout.activity_main)
        permissionLayout = findViewById(R.id.permission_layout)
        openPermissionBtn = findViewById(R.id.tv_open_permission)
        openPermissionBtn.setOnClickListener {
            requestMediaPermission()
        }
        mediaViewModel.selectedLiveData.observe(this) {
            showImageEditFragment()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!mediaPermissionGranted) {
            processContentShow()
        }
    }

    private fun processContentShow() {
        mediaPermissionGranted = PermissionUtils.permissionGranted(this, mediaPermission)
        if (mediaPermissionGranted) {
            showMediaFragment()
        } else {
            permissionLayout.isVisible = true
        }
    }

    private fun showImageEditFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.content, imageEditFragment).commit()
    }

    private fun showMediaFragment() {
        supportFragmentManager.beginTransaction().replace(R.id.content, mediaFragment).commit()
    }

    private fun requestMediaPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(mediaPermission)) {
            PermissionUtils.showGotoSettings(this, R.string.request_read_file_permission_desc)
        } else {
            readFilePermissionLauncher.launch(mediaPermission)
        }
    }

    override fun onBackPressed() {
        if (imageEditFragment.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }
}
