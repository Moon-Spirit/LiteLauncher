package com.litelauncher.app.managers

import android.content.Context
import com.litelauncher.app.bridge.ModInfo
import com.litelauncher.app.bridge.ModrinthProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipFile

/**
 * Mod 管理器 — 本地 Mod 扫描 + Modrinth API + 安装器
 */
class ModManager(private val context: Context) {
    private val modrinthApi = "https://api.modrinth.com/v2"

    suspend fun getInstalled(gameDir: String, loaderType: String): List<ModInfo> = withContext(Dispatchers.IO) {
        val modsDir = File(gameDir, "mods")
        if (!modsDir.exists()) return@withContext emptyList()

        modsDir.listFiles()?.filter { it.extension == "jar" }?.map { jar ->
            val meta = readJarMetadata(jar)
            ModInfo(
                fileName = jar.name,
                name = meta.first ?: jar.nameWithoutExtension,
                version = meta.second ?: "",
                loaderType = detectLoader(jar),
                enabled = !jar.name.endsWith(".disabled"),
                size = (jar.length() / 1024).toInt()
            )
        } ?: emptyList()
    }

    suspend fun searchModrinth(query: String, mcVersion: String, loaderType: String): List<ModrinthProject> =
        withContext(Dispatchers.IO) {
            val facets = """[["categories:$loaderType"],["versions:$mcVersion"],["project_type:mod"]]"""
            val url = URL("$modrinthApi/search?query=$query&facets=$facets&limit=20")

            val conn = url.openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "LiteLauncher/1.0 (sanjiu2024)")
            conn.connectTimeout = 10000
            conn.readTimeout = 15000

            val json = JSONObject(conn.inputStream.bufferedReader().readText())
            val hits = json.getJSONArray("hits")
            (0 until hits.length()).map {
                val h = hits.getJSONObject(it)
                ModrinthProject(
                    id = h.getString("project_id"),
                    slug = h.optString("slug", ""),
                    title = h.optString("title", ""),
                    description = h.optString("description", ""),
                    iconUrl = h.optString("icon_url", ""),
                    downloads = h.optInt("downloads", 0),
                    categories = listOf()
                )
            }
        }

    suspend fun installModrinth(projectId: String, versionId: String, gameDir: String) =
        withContext(Dispatchers.IO) {
            // 获取下载 URL
            val verUrl = "$modrinthApi/project/$projectId/version/$versionId"
            val conn = URL(verUrl).openConnection() as HttpURLConnection
            conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")

            val verJson = JSONObject(conn.inputStream.bufferedReader().readText())
            val files = verJson.getJSONArray("files")
            val downloadUrl = files.getJSONObject(0).getString("url")
            val filename = files.getJSONObject(0).getString("filename")

            // 下载到 mods 目录
            val modsDir = File(gameDir, "mods")
            modsDir.mkdirs()
            val dest = File(modsDir, filename)
            URL(downloadUrl).openStream().use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
        }

    suspend fun toggle(gameDir: String, fileName: String, enabled: Boolean) {
        val file = File(gameDir, "mods", fileName)
        if (enabled) {
            File(file.absolutePath.replace(".disabled", "")).also {
                if (it != file) file.renameTo(it)
            }
        } else {
            if (!file.name.endsWith(".disabled")) {
                file.renameTo(File(file.parent, "${file.name}.disabled"))
            }
        }
    }

    suspend fun installLoader(versionId: String, loaderType: String, loaderVersion: String) {
        // TODO: 下载并运行对应 Mod Loader 安装器 JAR
    }

    // 读取 JAR 内元数据
    private fun readJarMetadata(jar: File): Pair<String?, String?> {
        return try {
            ZipFile(jar).use { zip ->
                val fabricEntry = zip.getEntry("fabric.mod.json")
                if (fabricEntry != null) {
                    val json = JSONObject(zip.getInputStream(fabricEntry).bufferedReader().readText())
                    return Pair(json.optString("name", null), json.optString("version", null))
                }
                val forgeEntry = zip.getEntry("META-INF/mods.toml")
                if (forgeEntry != null) {
                    val content = zip.getInputStream(forgeEntry).bufferedReader().readText()
                    val modId = Regex("modId\\s*=\\s*\"([^\"]+)\"").find(content)?.groupValues?.get(1)
                    val version = Regex("version\\s*=\\s*\"([^\"]+)\"").find(content)?.groupValues?.get(1)
                    return Pair(modId, version)
                }
                Pair(null, null)
            }
        } catch (e: Exception) {
            Pair(null, null)
        }
    }

    // 检测 Mod 的 Loader 类型
    private fun detectLoader(jar: File): String {
        return try {
            ZipFile(jar).use { zip ->
                when {
                    zip.getEntry("quilt.mod.json") != null -> "quilt"
                    zip.getEntry("fabric.mod.json") != null -> "fabric"
                    zip.getEntry("META-INF/neoforge.mods.toml") != null -> "neoforge"
                    zip.getEntry("META-INF/mods.toml") != null -> "forge"
                    zip.getEntry("mcmod.info") != null -> "forge"
                    else -> "unknown"
                }
            }
        } catch (e: Exception) { "unknown" }
    }
}
