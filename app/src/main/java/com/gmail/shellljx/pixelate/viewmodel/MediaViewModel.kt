package com.gmail.shellljx.pixelate.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import com.gmail.shellljx.pixelate.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/7
 * @Description:
 */
class MediaViewModel : BaseViewModel() {

    companion object {
        private const val PAGE_SIZE = 50
    }

    private var page = 0
    var bucket: MediaBucket? = null
        set(value) {
            page = 0
            field = value
        }
    val mediasLiveData = MutableLiveData<List<MediaResource>>()
    val bucketLiveData = MutableLiveData<List<MediaBucket>>()
    val selectedLiveData = MutableLiveData<String>()
    @Volatile
    var loading = false
        private set
    @Volatile
    var hasMore = true
        private set

    fun fetchMedias(context: Context, forceReset: Boolean = false) {
        if (forceReset) {
            page = 0
        }
        loading = true
        launchSafely {
            val medias = loadMedias(context, bucket?.id)
            if (medias.isNotEmpty()) {
                mediasLiveData.postValue(medias)
                page++
            }
            loading = false
        }
    }

    fun fetchBuckets(context: Context) {
        launchSafely {
            val buckets = loadAlbum(context)
            if (buckets.isNotEmpty()) {
                bucketLiveData.postValue(buckets)
            }
        }
    }

    private suspend fun loadMedias(context: Context, bucketId: String? = null): List<MediaResource> {
        return withContext(Dispatchers.IO) {
            createMediaCursor(context, bucketId).use { cursor ->
                cursor?.let { queryMedias(it) } ?: emptyList()
            }
        }
    }

    private fun queryMedias(cursor: Cursor): List<MediaResource> {
        val idColumn = MediaStore.MediaColumns._ID
        val dataColumn = MediaStore.MediaColumns.DATA
        val displayNameColumn = MediaStore.MediaColumns.DISPLAY_NAME
        val mineTypeColumn = MediaStore.MediaColumns.MIME_TYPE
        val bucketIdColumn = MediaStore.MediaColumns.BUCKET_ID
        val bucketDisplayNameColumn = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        val list = arrayListOf<MediaResource>()
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
            val mediaResource = MediaResource(
                id = id,
                path = data,
                name = name,
                mimeType = mimeType,
                bucketId = bucketId,
                bucketName = bucketName
            )
            list.add(mediaResource)
        }
        hasMore = list.size >= PAGE_SIZE
        return list
    }

    private fun createMediaCursor(context: Context, bucketId: String? = null): Cursor? {
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
        val sortOrder = "$dateModifiedColumn DESC "

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

        if (bucketId != null) {
            selection.append(" AND ")
            selection.append(MediaStore.MediaColumns.BUCKET_ID)
            selection.append("='")
            selection.append(bucketId)
            selection.append("'")
        }
        val offset = PAGE_SIZE * page

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val queryArgs = Bundle()
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection.toString())
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_LIMIT, "$PAGE_SIZE OFFSET $offset")
            return context.contentResolver.query(
                contentUri,
                projection,
                queryArgs,
                null
            )
        } else {
            return context.contentResolver.query(
                contentUri,
                projection,
                selection.toString(),
                null,
                "$sortOrder LIMIT $PAGE_SIZE OFFSET $offset"
            )
        }
    }

    private suspend fun loadAlbum(context: Context): List<MediaBucket> {
        return withContext(Dispatchers.IO) {
            val dataColumn = MediaStore.MediaColumns.DATA
            val bucketIdColumn = MediaStore.MediaColumns.BUCKET_ID
            val bucketDisplayNameColumn = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
            val dateModifiedColumn = MediaStore.MediaColumns.DATE_MODIFIED
            val projection = arrayOf(
                dataColumn,
                bucketIdColumn,
                bucketDisplayNameColumn
            )
            val contentUri = MediaStore.Files.getContentUri("external")
            val sortOrder = "$dateModifiedColumn DESC "

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val queryArgs = Bundle()
                queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection.toString())
                queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, sortOrder)
                context.contentResolver.query(
                    contentUri,
                    projection,
                    queryArgs,
                    null
                )
            } else {
                context.contentResolver.query(
                    contentUri,
                    projection,
                    selection.toString(),
                    null,
                    sortOrder
                )
            }.use { cursor ->
                cursor?.let { queryBuckets(context, it) } ?: emptyList()
            }
        }
    }

    private fun queryBuckets(context: Context, cursor: Cursor): List<MediaBucket> {
        val dataColumn = MediaStore.MediaColumns.DATA
        val bucketIdColumn = MediaStore.MediaColumns.BUCKET_ID
        val bucketDisplayNameColumn = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
        val buckets = hashMapOf<String, MediaBucket>()
        var allBucket: MediaBucket? = null
        while (cursor.moveToNext()) {
            val data = cursor.getString(dataColumn, "")
            if (data.isBlank() || !File(data).exists()) {
                continue
            }
            val bucketId = cursor.getString(bucketIdColumn, "")
            val bucketName = cursor.getString(bucketDisplayNameColumn, "")
            if (allBucket == null) {
                allBucket = MediaBucket(null, context.getString(R.string.album_all), data, 1)
            } else {
                allBucket.count++
            }
            buckets[bucketId]?.let {
                it.count++
            } ?: run {
                buckets[bucketId] = MediaBucket(
                    bucketId,
                    bucketName,
                    data,
                    1
                )
            }
        }
        val list = buckets.values.toMutableList()
        allBucket?.let {
            list.add(0, it)
        }
        return list
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
        val path: String,
        val name: String,
        val mimeType: String,
    )

    data class MediaBucket(
        val id: String?,
        val name: String,
        val cover: String,
        var count: Int
    )
}