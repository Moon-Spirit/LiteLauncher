package com.litelauncher.app.managers

import com.litelauncher.app.bridge.VersionInfo
import com.litelauncher.app.bridge.VersionDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.File

/**
 * 版本管理器 — 获取 Mojang 版本清单 + 版本详情
 * 兼容旧格式 (1.x.x) 和新格式 (YY.D.H)
 */
class VersionManager {
    private val manifestUrl = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"
    private val cacheDir = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "litelauncher_cache")
    private val cacheFile = File(cacheDir, "version_manifest.json")

    suspend fun fetchVersions(): List<VersionInfo> = withContext(Dispatchers.IO) {
        val manifest = getManifest()
        val versions = mutableListOf<VersionInfo>()

        val json = JSONObject(manifest)
        val arr = json.getJSONArray("versions")
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            versions.add(VersionInfo(
                id = obj.getString("id"),
                type = obj.optString("type", "release"),
                url = obj.optString("url", ""),
                releaseTime = obj.optString("releaseTime", ""),
                installed = isInstalled(obj.getString("id")),
                loaderName = null
            ))
        }
        versions.sortedByDescending { parseVersionWeight(it.id) }
    }

    suspend fun getDetail(versionId: String): VersionDetail = withContext(Dispatchers.IO) {
        val manifest = JSONObject(getManifest())
        val arr = manifest.getJSONArray("versions")
        var detailUrl = ""

        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            if (obj.getString("id") == versionId) {
                detailUrl = obj.getString("url")
                break
            }
        }

        val versionJson = fetchJson(detailUrl)
        val json = JSONObject(versionJson)

        VersionDetail(
            id = json.getString("id"),
            type = json.optString("type", "release"),
            mainClass = json.optString("mainClass", "net.minecraft.client.main.Main"),
            assetsIndex = json.optJSONObject("assetIndex")?.optString("id") ?: "",
            javaVersion = json.optJSONObject("javaVersion")?.optInt("majorVersion") ?: 8,
            releaseTime = json.optString("releaseTime", ""),
            size = json.optJSONObject("downloads")?.optJSONObject("client")?.optInt("size") ?: 0
        )
    }

    // 根据版本号计算排序权重 (兼容新旧格式)
    private fun parseVersionWeight(versionId: String): Long {
        val parts = versionId.split(".")
        if (parts.size < 2) return 0

        return try {
            val major = parts[0].toLong()
            val minor = parts[1].toLong()
            val patch = if (parts.size > 2) extractNumber(parts[2]) else 0
            (major * 1000000) + (minor * 1000) + patch
        } catch (e: NumberFormatException) {
            0
        }
    }

    private fun extractNumber(s: String): Long {
        val num = s.filter { it.isDigit() }
        return if (num.isNotEmpty()) num.toLong() else 0
    }

    private fun isInstalled(versionId: String): Boolean {
        // TODO: 检查 versions/<id>/<id>.json 是否存在
        return false
    }

    private suspend fun getManifest(): String = withContext(Dispatchers.IO) {
        cacheDir.mkdirs()
        if (cacheFile.exists() && cacheFile.lastModified() > System.currentTimeMillis() - 86_400_000) {
            cacheFile.readText()
        } else {
            val data = fetchJson(manifestUrl)
            cacheFile.writeText(data)
            data
        }
    }

    private fun fetchJson(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000
        conn.readTimeout = 30000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")
        return conn.inputStream.bufferedReader().readText()
    }
}
