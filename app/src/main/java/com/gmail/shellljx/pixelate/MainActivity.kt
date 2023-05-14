package com.gmail.shellljx.pixelate

import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.math.MathUtils
import com.gmail.shellljx.pixelator.IRenderListener
import com.gmail.shellljx.pixelator.Pixelator

class MainActivity : AppCompatActivity() {
    val pixelator = Pixelator.create()
    lateinit var surfaceView: SurfaceView
    lateinit var gestureView: GestureView
    private var isWindowCreated = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById(R.id.surface_view)
        gestureView = findViewById(R.id.gesture_view)
        surfaceView.holder.addCallback(pixelator as? SurfaceHolder.Callback)
        gestureView.setGestureListener(object : GestureView.GestureListener {
            override fun onMove(points: List<PointF>) {
                if (!isWindowCreated) return
                val buffer = arrayListOf<Float>()
                points.forEach {
                    val openglX = MathUtils.clamp(it.x / (pixelator as Pixelator).width.toFloat() * 2f - 1, -1f,1f)
                    val openglY = MathUtils.clamp(it.y / pixelator.height.toFloat() * 2f - 1,-1f,1f)
                    buffer.add(openglX)
                    buffer.add(openglY)
                }
                pixelator.pushTouchBuffer(buffer.toFloatArray())
                pixelator.refreshFrame()
            }

            override fun onTranslate(scale: Float, angle: Float) {
                pixelator.translate(scale, 0f)
            }
        })
        pixelator.setRenderListener(object : IRenderListener {
            override fun onEGLContextCreate() {

            }

            override fun onEGLWindowCreate() {
                val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_brush_blur)
                pixelator.setBrush(bitmap)
                bitmap.recycle()
                pixelator.addImagePath("/sdcard/DCIM/Camera/下载.jpeg")
                isWindowCreated = true
            }
        })
    }
}
