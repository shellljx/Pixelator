package com.gmail.shellljx.pixelate.utils

import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import com.gmail.shellljx.pixelate.MyApplication
import java.io.*
import java.security.MessageDigest

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/7/22
 * @Description:
 */
object FileUtils {
    fun syncImageToGallery(context: Context, imageFileName: String) {
        val file = File(imageFileName)
        if (!file.exists()) {
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Pixelator") // 这里可以更改为您想要的相册目录
        }

        val contentResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        try {
            val newUri = contentResolver.insert(uri, values)
            if (newUri != null) {
                val inputStream = FileInputStream(file)
                val outputStream = contentResolver.openOutputStream(newUri)
                if (outputStream != null) {
                    inputStream.copyTo(outputStream)
                    outputStream.close()
                    inputStream.close()
                }
            }
        } catch (e: IOException) {
        }
    }

    fun getEffectDir(): String {
        return "${MyApplication.instance.cacheDir.absolutePath}/assets/effects"
    }

    fun getEffectName(url: String): String {
        return "${HashUtil.generateUniqueId(url)}.${File(url).extension}"
    }

    fun getEffectPath(url: String): String {
        return "${getEffectDir()}/${getEffectName(url)}"
    }
}