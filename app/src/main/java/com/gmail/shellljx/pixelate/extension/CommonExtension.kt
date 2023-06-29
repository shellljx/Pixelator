package com.gmail.shellljx.pixelate.extension

import android.graphics.Bitmap
import com.gmail.shellljx.pixelate.utils.DensityUtils
import com.gmail.shellljx.wrapper.IContainer
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

fun Int.dp(): Int {
    return DensityUtils.dip2px(this.toFloat())
}

fun Float.dp(): Int {
    return DensityUtils.dip2px(this)
}

fun launch(container: IContainer, block: suspend CoroutineScope.() -> Unit) {
    container.getLifeCycleService()?.launchSafely(block = block)
}

suspend fun Bitmap.writeToPngFile(parentPath: String, name: String, quality: Int): String? {
    return withContext(Dispatchers.IO) {
        val parent = File(parentPath)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val targetFile = File(parent, "$name.png")
        val stream = FileOutputStream(targetFile)
        try {
            val success = compress(Bitmap.CompressFormat.JPEG, quality, stream)
            if (success) {
                targetFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } finally {
            stream.flush()
            stream.close()
        }
    }
}