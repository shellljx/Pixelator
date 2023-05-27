package com.gmail.shellljx.pixelate

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.*
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.math.MathUtils
import com.gmail.shellljx.pixelate.panels.EffectsPanel
import com.gmail.shellljx.pixelator.IRenderListener
import com.gmail.shellljx.pixelator.Pixelator
import java.io.*

class MainActivity : AppCompatActivity() {
    val pixelator = Pixelator.create()
    lateinit var surfaceView: SurfaceView
    lateinit var gestureView: GestureView
    private var isWindowCreated = false
    private val serviceManager = ServiceManager(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getwcreenheight()
        setTransparent()
        val REQUEST_PERMISSION_CODE = 1
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查权限是否已被授予
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            ) {
                // 已经授予了读写权限
                // 可以进行读写操作
            } else {
                // 未授予读写权限，发起权限请求
                requestPermissions(permissions, REQUEST_PERMISSION_CODE)
            }
        }

        surfaceView = findViewById(R.id.surface_view)
        gestureView = findViewById(R.id.gesture_view)
        surfaceView.holder.addCallback(pixelator as? SurfaceHolder.Callback)
        gestureView.setGestureListener(object : GestureView.GestureListener {
            override fun onMove(points: List<PointF>) {
                if (!isWindowCreated) return
                val buffer = arrayListOf<Float>()
                points.forEach {
                    val openglX = MathUtils.clamp(
                        it.x / (pixelator as Pixelator).width.toFloat() * 2f - 1,
                        -1f,
                        1f
                    )
                    val openglY = MathUtils.clamp(it.y / pixelator.height.toFloat() * 2f, -1f, 1f)
                    buffer.add(it.x)
                    buffer.add(it.y)
                }
                pixelator.pushTouchBuffer(buffer.toFloatArray())
                pixelator.refreshFrame()
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
                val path = "/sdcard/aftereffect/ae/tt/resource/assets/a11.jpg"
                pixelator.addImagePath(path, getRotate(path))
                isWindowCreated = true

                surfaceView.post {
                    serviceManager.miniScreenPanel.onCreateView(findViewById(R.id.layout_miniscreen))
                }
            }

            override fun onFrameBoundsChanged(
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ) {
                gestureView.onFrameBoundsChanged(left, top, right, bottom)
            }

            override fun onFrameSaved(bitmap: Bitmap) {
                saveBitmap(bitmap)
            }
        })
        findViewById<TextView>(R.id.tv_save).setOnClickListener {
            pixelator.save()
        }
        serviceManager.miniScreenPanel.imageSdk = pixelator
        serviceManager.effectPanel.onViewCreated()
        serviceManager.start()
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
            when (exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }

    private fun getwcreenheight() {
        val manager = this.windowManager
        val outMetrics = DisplayMetrics()
        manager.defaultDisplay.getMetrics(outMetrics)
        val width2 = outMetrics.widthPixels
        val height2 = outMetrics.heightPixels
    }

    fun setTransparent() {
        transparentStatusBar(this)
        hintNavigationBar(this)
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

    private fun setRootView(activity: Activity) {
        val parent = activity.findViewById<View>(android.R.id.content) as ViewGroup
        var i = 0
        val count = parent.childCount
        while (i < count) {
            val childView = parent.getChildAt(i)
            if (childView is ViewGroup) {
                childView.setFitsSystemWindows(true)
                childView.clipToPadding = true
            }
            i++
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
