package com.gmail.shellljx.pixelate

import android.graphics.*
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
                    val openglY = MathUtils.clamp(it.y / pixelator.height.toFloat() * 2f, -1f, 1f)
                    buffer.add(it.x)
                    buffer.add(it.y)
                }
                pixelator.pushTouchBuffer(buffer.toFloatArray())
                pixelator.refreshFrame()
            }

            override fun onTranslate(scale: Float, pivotX: Float, pivotY: Float, angle: Float, translateX: Float, translateY: Float) {
                pixelator.translate(scale, pivotX, pivotY, 0f, translateX, translateY)
            }

            override fun refresh(matrix: Matrix) {
                val v = FloatArray(9)
                matrix.getValues(v)
                val glmArray = floatArrayOf(
                    v[0], v[3], 0f, 0f,
                    v[1], v[4], 0f, 0f,
                    0f, 0f, 1f, 0f,
                    v[2], v[5], 0f, 1f
                )
                pixelator.setMatrix(glmArray)
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

            override fun onFrameAvaliable(x: Int, y: Int, width: Int, height: Int) {
                gestureView.initFrame(x, y, width, height)
                val v = FloatArray(9)
                gestureView.transformMatrix.getValues(v)
                val glmArray = floatArrayOf(
                    v[0], v[3], 0f, 0f,
                    v[1], v[4], 0f, 0f,
                    0f, 0f, 1f, 0f,
                    v[2], v[5], 0f, 1f
                )
                pixelator.setMatrix(glmArray)
                pixelator.refreshFrame()
            }
        })
        findViewById<Switch>(R.id.editswitch).setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                gestureView.editEnable = p1
            }
        })
    }
}
