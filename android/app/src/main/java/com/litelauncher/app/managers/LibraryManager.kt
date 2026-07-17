package com.litelauncher.app.managers

import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * 库管理器 — 解析 version JSON 的 libraries 数组, 下载 + 提取 natives
 */
class LibraryManager(private val onProgress: (String, Float) -> Unit) {

    data class LibraryEntry(val name: String, val path: String, val url: String, val sha1: String?, val isNative: Boolean, val extractRules: List<String>)

    suspend fun install(gameDir: String, versionJson: JSONObject, useBmclapi: Boolean) = withContext(Dispatchers.IO) {
        val libraries = versionJson.optJSONArray("libraries") ?: JSONArray()
        val entries = mutableListOf<LibraryEntry>()
        val total = libraries.length()

        // 1. 解析所有库条目
        for (i in 0 until total) {
            val lib = libraries.getJSONObject(i)
            val name = lib.getString("name")
            val downloads = lib.optJSONObject("downloads") ?: continue

            // 检查平台规则
            val rules = lib.optJSONArray("rules")
            if (rules != null && !matchesAndroid(rules)) continue

            // 主 JAR
            val artifact = downloads.optJSONObject("artifact")
            if (artifact != null) {
                entries.add(LibraryEntry(
                    name, artifact.getString("path"),
                    if (useBmclapi) toBmclapi(artifact.getString("url")) else artifact.getString("url"),
                    artifact.optString("sha1", null), false, listOf()
                ))
            }

            // Natives
            val natives = lib.optJSONObject("natives")
            if (natives != null) {
                val classifier = natives.optString("linux", null) ?: natives.optString("osx", null) ?: continue
                val classifiers = downloads.optJSONObject("classifiers")
                if (classifiers != null) {
                    val nativeArtifact = classifiers.optJSONObject(classifier)
                    if (nativeArtifact != null) {
                        val extract = lib.optJSONObject("extract")?.optJSONArray("exclude")?.let { arr ->
                            (0 until arr.length()).map { arr.getString(it) }
                        } ?: listOf("META-INF/")
                        entries.add(LibraryEntry(
                            "$name:$classifier", nativeArtifact.getString("path"),
                            if (useBmclapi) toBmclapi(nativeArtifact.getString("url")) else nativeArtifact.getString("url"),
                            nativeArtifact.optString("sha1", null), true, extract
                        ))
                    }
                }
            }
        }

        // 2. 下载
        val libsDir = File(gameDir, "libraries")
        val nativesDir = File(gameDir, "natives")
        nativesDir.mkdirs()

        for ((index, entry) in entries.withIndex()) {
            val dest = File(libsDir, entry.path)
            if (!dest.exists()) download(entry.url, dest)

            // 验证 SHA1
            if (entry.sha1 != null && dest.exists()) {
                val actual = sha1(dest)
                if (!actual.equals(entry.sha1, ignoreCase = true)) {
                    dest.delete(); download(entry.url, dest)
                }
            }

            // 提取 natives
            if (entry.isNative) {
                extractNatives(dest, nativesDir, entry.extractRules)
            }

            onProgress("libraries", (index + 1).toFloat() / entries.size.toFloat())
        }
    }

    private fun matchesAndroid(rules: JSONArray): Boolean {
        for (i in 0 until rules.length()) {
            val rule = rules.getJSONObject(i)
            val os = rule.optJSONObject("os")
            val action = rule.optString("action", "allow")
            if (os != null && os.optString("name") == "linux" && action == "allow") return true
        }
        return false
    }

    private suspend fun download(url: String, dest: File) = withContext(Dispatchers.IO) {
        dest.parentFile?.mkdirs()
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10000; conn.readTimeout = 120000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")
        conn.inputStream.use { FileOutputStream(dest).use { it.copyTo(it) } }
    }

    private fun extractNatives(jar: File, destDir: File, exclude: List<String>) {
        java.util.zip.ZipFile(jar).use { zip ->
            zip.entries().asIterator().forEach { entry ->
                if (exclude.any { entry.name.startsWith(it) }) return@forEach
                if (entry.isDirectory) return@forEach
                val f = File(destDir, entry.name)
                f.parentFile?.mkdirs()
                zip.getInputStream(entry).use { FileOutputStream(f).use { it.copyTo(it) } }
            }
        }
    }

    private fun sha1(file: File): String {
        val digest = MessageDigest.getInstance("SHA-1")
        file.inputStream().use { input ->
            val buf = ByteArray(8192)
            var n: Int
            while (input.read(buf).also { n = it } != -1) digest.update(buf, 0, n)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        fun toBmclapi(url: String) = url
            .replace("https://libraries.minecraft.net", "https://bmclapi2.bangbang93.com/libraries")
            .replace("https://maven.minecraftforge.net", "https://bmclapi2.bangbang93.com/maven")
            .replace("https://maven.fabricmc.net", "https://bmclapi2.bangbang93.com/maven")
    }
}
