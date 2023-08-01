package com.gmail.shellljx.imagePicker

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.resume

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/7/31
 * @Description:
 */

object MediaLoader {

    private const val COROUTINE_TAG = "MediaLoader"
    private var loadJob: Job? = null

    private val bucketCache = arrayListOf<MediaBucket>()

    fun load(context: Context, onComplete: (List<MediaBucket>) -> Unit) {
        if (bucketCache.isNotEmpty()) {
            onComplete.invoke(bucketCache)
            return
        }
        if (loadJob?.isActive == true) return
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        }
        loadJob = CoroutineScope(CoroutineName(COROUTINE_TAG) + exceptionHandler).launch {
            val medias = loadMedia(context)
            if (medias.isNullOrEmpty()) {
                return@launch
            }
            val buckets = loadMediaBucket(medias)
            withContext(Dispatchers.Main) {
                bucketCache.clear()
                bucketCache.addAll(buckets)
                onComplete.invoke(bucketCache)
            }
        }
    }

    private suspend fun loadMediaBucket(medias: List<MediaResource>): List<MediaBucket> {
        return suspendCancellableCoroutine { continuation ->
            val bucketMap = hashMapOf<String, MediaBucket>()
            medias.forEach { res ->
                if (res.bucketName.isNotBlank()) {
                    val bucketId = res.bucketId
                    var bucket = bucketMap[bucketId]
                    if (bucket == null) {
                        bucket = MediaBucket(bucketId, res.bucketName, mutableListOf())
                        bucketMap[bucketId] = bucket
                    }
                    bucket.resources.add(res)
                }
            }

            val bucketList = arrayListOf<MediaBucket>()
            bucketList.add(MediaBucket("", "全部", medias.toMutableList()))
            bucketList.addAll(bucketMap.values.sortedBy { it.name })
            continuation.resume(bucketList)
        }
    }

    private suspend fun loadMedia(context: Context): List<MediaResource>? {
        return suspendCancellableCoroutine { continuation ->
            var mediaCursor: Cursor? = null
            continuation.invokeOnCancellation {
                mediaCursor?.close()
            }
            val idColumn = MediaStore.MediaColumns._ID
            val dataColumn = MediaStore.MediaColumns.DATA
            val displayNameColumn = MediaStore.MediaColumns.DISPLAY_NAME
            val mineTypeColumn = MediaStore.MediaColumns.MIME_TYPE
            val bucketIdColumn = MediaStore.MediaColumns.BUCKET_ID
            val bucketDisplayNameColumn = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
            val dateModifiedColumn = MediaStore.MediaColumns.DATE_MODIFIED
            val projection = arrayOf(
                idColumn,
                dataColumn,
                displayNameColumn,
                mineTypeColumn,
                bucketIdColumn,
                bucketDisplayNameColumn
            )
            val contentUri = MediaStore.Files.getContentUri("external")
            val sortOrder = "$dateModifiedColumn DESC"
            val mediaResourceList = mutableListOf<MediaResource>()

            val selection = StringBuilder()
            selection.append(MediaStore.MediaColumns.MIME_TYPE)
            selection.append(" IN (")
            val mimeTypes = arrayListOf("image/jpeg", "image/png", "image/jpg")
            mimeTypes.forEachIndexed { index, mimeType ->
                if (index != 0) {
                    selection.append(",")
                }
                selection.append("'")
                selection.append(mimeType)
                selection.append("'")
            }
            selection.append(")")

            try {
                mediaCursor = context.contentResolver.query(
                    contentUri,
                    projection,
                    selection.toString(),
                    null,
                    sortOrder,
                )
                mediaCursor?.let { cursor ->
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn, Long.MAX_VALUE)
                        val data = cursor.getString(dataColumn, "")
                        if (id == Long.MAX_VALUE || data.isBlank() || !File(data).exists()) {
                            continue
                        }
                        val name = cursor.getString(displayNameColumn, "")
                        val mimeType = cursor.getString(mineTypeColumn, "")
                        val bucketId = cursor.getString(bucketIdColumn, "")
                        val bucketName = cursor.getString(bucketDisplayNameColumn, "")
                        val uri = ContentUris.withAppendedId(contentUri, id)
                        val mediaResource = MediaResource(
                            id = id,
                            path = data,
                            uri = uri,
                            name = name,
                            mimeType = mimeType,
                            bucketId = bucketId,
                            bucketName = bucketName
                        )
                        mediaResourceList.add(element = mediaResource)
                    }
                    continuation.resume(mediaResourceList)
                } ?: run {
                    continuation.resume(null)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                continuation.resume(null)
            } finally {
                mediaCursor?.close()
            }
        }
    }

    private fun Cursor.getLong(columnName: String, default: Long): Long {
        return try {
            val columnIndex = getColumnIndexOrThrow(columnName)
            getLong(columnIndex)
        } catch (e: Throwable) {
            e.printStackTrace()
            default
        }
    }

    private fun Cursor.getString(columnName: String, default: String): String {
        return try {
            val columnIndex = getColumnIndexOrThrow(columnName)
            getString(columnIndex) ?: default
        } catch (e: Throwable) {
            e.printStackTrace()
            default
        }
    }

    data class MediaResource(
        internal val id: Long,
        internal val bucketId: String,
        internal val bucketName: String,
        val uri: Uri,
        val path: String,
        val name: String,
        val mimeType: String,
    )

    data class MediaBucket(
        val id: String,
        val name: String,
        val resources: MutableList<MediaResource>
    )
}
