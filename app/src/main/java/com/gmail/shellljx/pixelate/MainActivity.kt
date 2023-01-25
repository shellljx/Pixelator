package com.gmail.shellljx.pixelate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.gmail.shellljx.pixelator.IRenderListener
import com.gmail.shellljx.pixelator.Pixelator

class MainActivity : AppCompatActivity() {
    val pixelator = Pixelator.create()
    lateinit var surfaceView: SurfaceView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(pixelator as? SurfaceHolder.Callback)
        pixelator.setRenderListener(object : IRenderListener {
            override fun onEGLContextCreate() {

            }

            override fun onEGLWindowCreate() {
                pixelator.addImagePath("/sdcard/aftereffects/ae2/冬日最佳拍档/resource/assets/asset10.png")
                surfaceView.post {
                    pixelator.touchEvent(200f, 200f)
                    pixelator.touchEvent(300f, 300f)
                    pixelator.refreshFrame()
                    pixelator.touchEvent(400f, 400f)
                    pixelator.refreshFrame()
                }
            }
        })
    }
}
