package com.gmail.shellljx.pixelate.extension

import android.graphics.Bitmap
import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelate.STATUS
import com.gmail.shellljx.pixelate.utils.DensityUtils
import com.gmail.shellljx.pixelate.utils.FileUtils
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

fun List<EffectItem>.fill(): List<EffectItem> {
    filter { it.url.isNotEmpty() }.forEach {
        it.fill()
    }
    return this
}

fun EffectItem.fill() {
    val localPath = FileUtils.getEffectPath(url)
    if (File(localPath).exists()) {
        path = localPath
        status = STATUS.Downloaded
    } else {
        status = STATUS.NotDownload
    }
}