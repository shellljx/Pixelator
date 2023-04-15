package com.gmail.shellljx.pixelate

import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.gmail.shellljx.pixelator.IRenderListener
import com.gmail.shellljx.pixelator.Pixelator

class MainActivity : AppCompatActivity() {
    val pixelator = Pixelator.create()
    lateinit var surfaceView: SurfaceView
    lateinit var gestureView: GestureView
    private var isWindowCreated = false
    private var prePoint = PointF()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById(R.id.surface_view)
        gestureView = findViewById(R.id.gesture_view)
        surfaceView.holder.addCallback(pixelator as? SurfaceHolder.Callback)
        gestureView.setGestureListener(object : GestureView.GestureListener {
            override fun onMove(points: List<PointF>) {
                if (!isWindowCreated) return
                System.out.println("lijinxiang size ${points.size}")
                points.forEach {
                    pixelator.touchEvent(it.x, it.y)
                }
                pixelator.refreshFrame()
            }
        })
        pixelator.setRenderListener(object : IRenderListener {
            override fun onEGLContextCreate() {

            }

            override fun onEGLWindowCreate() {
                val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_brush_blur)
                pixelator.setBrush(bitmap)
                bitmap.recycle()
                pixelator.addImagePath("/sdcard/aftereffect/ae/asset11.png")
                isWindowCreated = true
            }
        })
    }
}
