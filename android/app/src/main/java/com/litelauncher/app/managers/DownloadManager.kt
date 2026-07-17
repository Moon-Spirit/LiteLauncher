package com.litelauncher.app.managers

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 下载管理器 — 版本/资源/库/JRE 下载 + 进度回调
 */
class DownloadManager(private val context: Context) {
    private val _tasks = MutableStateFlow<Map<String, DownloadState>>(emptyMap())
    val tasks: StateFlow<Map<String, DownloadState>> = _tasks

    data class DownloadState(
        val name: String,
        val url: String,
        val dest: File,
        val totalBytes: Long,
        val downloadedBytes: Long,
        val speed: Long,
        val status: String // pending, downloading, paused, completed, error
    )

    suspend fun installVersion(versionId: String) {
        // TODO: 完整下载流程
        // 1. 下载 version JSON
        // 2. 下载 client.jar
        // 3. 下载 libraries
        // 4. 下载 assets
        // 5. 提取 natives
    }

    suspend fun cancel(taskId: String) {
        // TODO: 取消下载
        _tasks.value = _tasks.value.toMutableMap().apply { remove(taskId) }
    }

    private suspend fun download(url: String, dest: File, name: String): Boolean {
        dest.parentFile?.mkdirs()
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 120000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")

        val total = conn.contentLength.toLong()
        val buffer = ByteArray(8192)
        var downloaded = 0L

        conn.inputStream.use { input ->
            FileOutputStream(dest).use { output ->
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    downloaded += bytesRead
                }
            }
        }
        return dest.length() >= total || total <= 0
    }

    // BMCLAPI 镜像切换
    fun toBmclapi(url: String): String {
        return url
            .replace("https://launchermeta.mojang.com", "https://bmclapi2.bangbang93.com")
            .replace("https://piston-meta.mojang.com", "https://bmclapi2.bangbang93.com")
            .replace("https://piston-data.mojang.com", "https://bmclapi2.bangbang93.com")
            .replace("https://libraries.minecraft.net", "https://bmclapi2.bangbang93.com/libraries")
            .replace("https://resources.download.minecraft.net", "https://bmclapi2.bangbang93.com/assets")
    }
}
