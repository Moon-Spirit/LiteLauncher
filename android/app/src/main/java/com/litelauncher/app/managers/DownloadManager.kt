package com.litelauncher.app.managers

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class DownloadManager {
    data class DownloadState(val name: String, val progress: Float, val status: String)

    suspend fun installVersion(versionId: String, versionUrl: String, gameDir: String): Boolean = withContext(Dispatchers.IO) {
        val useBmclapi = Locale.getDefault().country == "CN"
        try {
            val jsonUrl = if (useBmclapi) AssetManager.toBmclapi(versionUrl) else versionUrl
            val versionDir = File(gameDir, "versions/$versionId")
            versionDir.mkdirs()
            val jsonFile = File(versionDir, "$versionId.json")
            download(jsonUrl, jsonFile)
            val versionJson = JSONObject(jsonFile.readText())

            val clientUrl = versionJson.getJSONObject("downloads").getJSONObject("client").getString("url")
            download(clientUrl, File(versionDir, "$versionId.jar"))

            LibraryManager { _, _ -> }.install(gameDir, versionJson, useBmclapi)
            AssetManager { _, _ -> }.install(gameDir, versionJson, useBmclapi)
            true
        } catch (e: Exception) { e.printStackTrace(); false }
    }

    fun cancel(taskId: String) {}

    private fun download(url: String, dest: File) {
        if (dest.exists() && dest.length() > 0) return
        dest.parentFile?.mkdirs()
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000; conn.readTimeout = 120000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")
        conn.inputStream.use { FileOutputStream(dest).use { it.copyTo(it) } }
    }
}
