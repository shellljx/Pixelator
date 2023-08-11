package com.gmail.shellljx.pixelate.viewmodel

import androidx.lifecycle.MutableLiveData
import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelate.STATUS
import com.gmail.shellljx.pixelate.extension.fill
import com.gmail.shellljx.pixelate.utils.FileUtils
import com.gmail.shellljx.pixelate.utils.OKDownloaderListener
import com.liulishuo.okdownload.DownloadTask
import com.liulishuo.okdownload.core.cause.EndCause
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.InputStream
import java.io.OutputStream

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/10
 * @Description:
 */
class EffectViewModel : BaseViewModel() {
    companion object {
        private const val ASSETS_URL = "https://storage.googleapis.com/app_assetss/assets.json"
        private const val VERSION_KEY = "x-goog-meta-asset-version"
    }

    private val client = OkHttpClient()
    val effectsLiveData = MutableLiveData<List<EffectItem>>()
    val downloadLiveData = MutableLiveData<DownloadResult>()

    fun fetch(path: String) {
        launchSafely {
            var version = -1
            val file = File(path)
            if (file.exists() && file.length() > 0) {
                val pair = parseAssets(path)
                version = pair.first
                effectsLiveData.postValue(pair.second.fill())
            }
            val success = requestAssets(ASSETS_URL, version, path)
            if (success) {
                val pair = parseAssets(path)
                effectsLiveData.postValue(pair.second.fill())
            }
        }
    }

    fun downloadEffect(position: Int, url: String, dstPath: String) {
        val task = DownloadTask.Builder(url, File(dstPath))
            .setFilename(FileUtils.getEffectName(url))
            .setMinIntervalMillisCallbackProcess(64)
            .setPassIfAlreadyCompleted(false)
            .build()
        task.enqueue(object : OKDownloaderListener() {
            override fun taskEnd(task: DownloadTask, cause: EndCause, realCause: java.lang.Exception?) {
                if (task.file?.exists() == true) {
                    val loaded = DownloadResult(position, STATUS.Downloaded)
                    downloadLiveData.postValue(loaded)
                } else {
                    val error = DownloadResult(position, STATUS.Downloaded)
                    downloadLiveData.postValue(error)
                }
            }
        })
    }

    private suspend fun requestAssets(url: String, version: Int, path: String): Boolean {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val remoteVersion = response.headers[VERSION_KEY]?.toInt() ?: -1
                if (remoteVersion == version) {
                    response.close()
                    return@withContext false
                }
                var inputStream: InputStream? = null
                var outputStream: OutputStream? = null
                try {
                    inputStream = response.body?.byteStream()
                    val dstFile = File(path)
                    if (dstFile.parentFile?.exists() != true) {
                        dstFile.parentFile?.mkdirs()
                    }
                    outputStream = FileOutputStream(dstFile)

                    val buffer = ByteArray(4096)
                    var bytesRead: Int

                    while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    return@withContext true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                } finally {
                    outputStream?.close()
                    inputStream?.close()
                }
            } else {
                false
            }
        }
    }

    private suspend fun parseAssets(assets: String?): Pair<Int, List<EffectItem>> {
        assets ?: return Pair(-1, emptyList())
        return withContext(Dispatchers.IO) {
            try {
                val jsonObj = JSONObject(FileReader(assets).readText())
                val version = jsonObj.getInt("version")
                val array = jsonObj.getJSONArray("effects")
                val effects = arrayListOf<EffectItem>()
                for (index in 0 until array.length()) {
                    val asset = array.getJSONObject(index)
                    val type = asset.getInt("type")
                    val url = asset.getString("url")
                    val cover = asset.getString("cover")
                    effects.add(EffectItem(index, type, cover, url))
                }
                Pair(version, effects)
            } catch (e: Exception) {
                e.printStackTrace()
                Pair(-1, emptyList())
            }
        }
    }

    data class DownloadResult(val position: Int, val status: STATUS)
}