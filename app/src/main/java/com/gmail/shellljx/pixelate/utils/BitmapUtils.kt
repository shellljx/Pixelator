package com.gmail.shellljx.pixelate.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/29
 * @Description:
 */
object BitmapUtils {

    suspend fun decodeBitmap(path: String, maxBounds: Int = 1080): Bitmap {
        return withContext(Dispatchers.IO) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            val originalWidth = options.outWidth
            val originalHeight = options.outHeight
            var targetWidth = originalWidth
            var targetHeight = originalHeight
            var inSampleSize = 1
            while (targetWidth > maxBounds || targetHeight > maxBounds) {
                inSampleSize *= 2
                targetWidth /= inSampleSize
                targetHeight /= inSampleSize
            }
            options.inJustDecodeBounds = false
            options.inSampleSize = 1
            BitmapFactory.decodeFile(path, options)
        }
    }
}