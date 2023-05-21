package com.gmail.shellljx.pixelate

import android.graphics.*
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.Switch
import android.widget.TextView
import androidx.core.math.MathUtils
import com.gmail.shellljx.pixelator.IRenderListener
import com.gmail.shellljx.pixelator.Pixelator
import java.io.*

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
                val path = "/sdcard/DCIM/Camera/20230520_202707.jpg"
                pixelator.addImagePath(path, getRotate(path))
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
                //pixelator.setMatrix(glmArray)
                //pixelator.refreshFrame()
            }

            override fun onFrameSaved(bitmap: Bitmap) {
                saveBitmap(bitmap)
            }
        })
        findViewById<Switch>(R.id.editswitch).setOnCheckedChangeListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                gestureView.editEnable = p1
            }
        })
        findViewById<TextView>(R.id.savebutton).setOnClickListener {
            pixelator.save()
        }
    }

    private fun saveBitmap(bitmap: Bitmap) {
        val file = File("/sdcard/DCIM/Camera/lijinxiang.png")

// 创建文件输出流
        var outStream: FileOutputStream? = null
        try {
            outStream = FileOutputStream(file)

            // 将Bitmap压缩为PNG格式，并将其写入文件输出流
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)

            // 刷新并关闭输出流
            outStream.flush()
            outStream.close()

            // 保存成功
            // 进行其他操作或显示成功消息
        } catch (e: IOException) {
            e.printStackTrace()
            // 处理IO异常
        } finally {
            // 确保关闭输出流
            outStream?.close()
        }
    }

    private fun getRotate(path: String): Int {
        return try {
            val exifInterface = ExifInterface(path)
            when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}
