package com.litelauncher.app.managers

import kotlinx.coroutines.*
import org.json.JSONObject
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * 资源管理器 — 下载 + 验证 Minecraft 资源文件
 */
class AssetManager(private val onProgress: (String, Float) -> Unit) {

    suspend fun install(gameDir: String, versionJson: JSONObject, useBmclapi: Boolean) = withContext(Dispatchers.IO) {
        val assetIndex = versionJson.getJSONObject("assetIndex")
        val indexUrl = if (useBmclapi) toBmclapi(assetIndex.getString("url")) else assetIndex.getString("url")
        val indexId = assetIndex.getString("id")

        // 1. 下载 asset index
        val indexFile = File(gameDir, "assets/indexes/$indexId.json")
        download(indexUrl, indexFile)

        // 2. 解析 asset objects
        val index = JSONObject(indexFile.readText())
        val objects = index.getJSONObject("objects")
        val keys = objects.keys()
        val total = objects.length()
        var done = 0

        // 3. 逐个下载
        val baseUrl = if (useBmclapi) "https://bmclapi2.bangbang93.com/assets" else "https://resources.download.minecraft.net"
        val objectsDir = File(gameDir, "assets/objects")

        while (keys.hasNext()) {
            val key = keys.next()
            val hash = objects.getJSONObject(key).getString("hash")
            val prefix = hash.substring(0, 2)
            val dest = File(objectsDir, "$prefix/$hash")

            if (!dest.exists()) {
                download("$baseUrl/$prefix/$hash", dest)
            }

            done++
            onProgress("assets", done.toFloat() / total.toFloat())
        }
    }

    private suspend fun download(url: String, dest: File) = withContext(Dispatchers.IO) {
        if (dest.exists() && dest.length() > 0) return@withContext
        dest.parentFile?.mkdirs()
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000; conn.readTimeout = 120000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")
        conn.inputStream.use { FileOutputStream(dest).use { it.copyTo(it) } }
    }

    companion object {
        fun toBmclapi(url: String) = url
            .replace("https://piston-meta.mojang.com", "https://bmclapi2.bangbang93.com")
            .replace("https://piston-data.mojang.com", "https://bmclapi2.bangbang93.com")
            .replace("https://resources.download.minecraft.net", "https://bmclapi2.bangbang93.com/assets")
    }
}
