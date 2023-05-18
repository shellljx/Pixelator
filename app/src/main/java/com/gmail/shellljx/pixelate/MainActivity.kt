package com.gmail.shellljx.pixelate

import android.graphics.BitmapFactory
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Switch
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
                    val openglX = MathUtils.clamp(it.x / (pixelator as Pixelator).width.toFloat() * 2f - 1, -1f, 1f)
                    val openglY = MathUtils.clamp(1 - it.y / pixelator.height.toFloat() * 2f, -1f, 1f)
                    buffer.add(it.x)
                    buffer.add(pixelator.height.toFloat() - it.y)
                }
                pixelator.pushTouchBuffer(buffer.toFloatArray())
                pixelator.refreshFrame()
            }

            override fun onTranslate(scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float) {
                pixelator.translate(scale, pivotX, pivotY, 0f, translateX, translateY)
            }
        })
        pixelator.setRenderListener(object : IRenderListener {
            override fun onEGLContextCreate() {

            }

            override fun onEGLWindowCreate() {
                val bitmap = BitmapFactory.decodeResource(resources, R.mipmap.ic_brush_blur)
                pixelator.setBrush(bitmap)
                bitmap.recycle()
                pixelator.addImagePath("/sdcard/aftereffect/ae/tt/resource/assets/a1.png")
                isWindowCreated = true
            }
        })
        findViewById<Switch>(R.id.editswitch).setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                gestureView.editEnable = p1
            }
        })
    }
}
