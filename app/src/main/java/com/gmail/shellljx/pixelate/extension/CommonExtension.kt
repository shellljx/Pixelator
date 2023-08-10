package com.gmail.shellljx.pixelate.extension

import android.graphics.Bitmap
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.gmail.shellljx.pixelate.utils.DensityUtils
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.AbsService
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.reflect.KClass

fun Int.dp(): Int {
    return DensityUtils.dip2px(this.toFloat())
}

fun Float.dp(): Int {
    return DensityUtils.dip2px(this)
}

suspend fun Bitmap.writeToPngFile(targetPath: String, quality: Int): Boolean {
    return withContext(Dispatchers.IO) {
        val targetFile = File(targetPath)
        if (targetFile.parentFile?.exists() != true) {
            targetFile.parentFile?.mkdirs()
        }
        val stream = FileOutputStream(targetFile)
        try {
            compress(Bitmap.CompressFormat.PNG, quality, stream)
        } catch (e: Exception) {
            false
        } finally {
            stream.flush()
            stream.close()
        }
    }
}