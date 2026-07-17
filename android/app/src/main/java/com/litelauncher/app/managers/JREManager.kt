package com.litelauncher.app.managers

import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * JRE 管理器 — 下载 Pojav 特制 Android JRE
 * PojavLauncher 的 android-openjdk-build-multiarch 构建
 * 为 Android bionic libc 编译，包含 libjli.so / libjvm.so
 */
class JREManager(private val onProgress: (String, Float) -> Unit) {

    data class JREInfo(val majorVersion: Int, val artifact: String, val dir: String, val url: String)

    // Pojav JRE — GitHub Releases, BMCLAPI 镜像
    private val baseUrl = "https://github.com/PojavLauncherTeam/android-openjdk-build-multiarch/releases/download"
    private val bmclapiUrl = "https://bmclapi2.bangbang93.com/mirrors/pojav/jre"

    private val jreList = listOf(
        JREInfo(8,  "jre8-aarch64",    "jre-8",  ""),
        JREInfo(17, "jre17-aarch64",   "jre-17", ""),
        JREInfo(21, "jre21-aarch64",   "jre-21", ""),
        JREInfo(25, "jre25-aarch64",   "jre-25", ""),
    )

    /**
     * 下载 Pojav 特制 Android JRE
     * @param requiredVersion MC 版本对应的 majorVersion (8/17/21/25)
     * @param dataDir 应用数据目录
     */
    suspend fun ensureJRE(requiredVersion: Int, dataDir: String): String = withContext(Dispatchers.IO) {
        val jre = jreList.firstOrNull { it.majorVersion == requiredVersion }
            ?: throw IllegalArgumentException("不支持的 Java 版本: $requiredVersion")

        val jreDir = File(dataDir, "jre/${jre.dir}")
        val javaBin = File(jreDir, "bin/java")
        val releaseFile = File(jreDir, "release")

        // 已安装 — 验证 release 文件中的 JAVA_VERSION
        if (javaBin.exists() && releaseFile.exists()) {
            val releaseContent = releaseFile.readText()
            if (releaseContent.contains("JAVA_VERSION=\"1.$requiredVersion") ||
                releaseContent.contains("JAVA_VERSION=\"$requiredVersion")) {
                onProgress("jre-$requiredVersion", 1.0f)
                return@withContext jreDir.absolutePath
            }
        }

        // 下载 tar.xz (Pojav 格式)
        val tarFile = File(dataDir, "jre/${jre.artifact}.tar.xz")
        tarFile.parentFile?.mkdirs()

        // BMCLAPI 镜像优先后回退 GitHub
        val mirrorUrl = "$bmclapiUrl/${jre.artifact}.tar.xz"
        val githubUrl = "$baseUrl/${jre.artifact}/${jre.artifact}.tar.xz"

        val success = try {
            download(mirrorUrl, tarFile, "jre-$requiredVersion")
        } catch (e: Exception) {
            tarFile.delete()
            try { download(githubUrl, tarFile, "jre-$requiredVersion") } catch (e2: Exception) { false }
        }

        if (!success || tarFile.length() < 10_000_000) {
            throw RuntimeException("JRE 下载失败: ${jre.artifact}")
        }

        // 解压 tar.xz
        jreDir.mkdirs()
        extractTarXz(tarFile, jreDir)
        tarFile.delete()

        // 验证
        if (!javaBin.exists()) {
            // 可能解压到了一个子目录 — 移动内容
            jreDir.listFiles()?.firstOrNull { it.isDirectory && File(it, "bin/java").exists() }?.let { inner ->
                inner.listFiles()?.forEach { it.renameTo(File(jreDir, it.name)) }
                inner.delete()
            }
        }

        javaBin.setExecutable(true)
        jreDir.absolutePath
    }

    private suspend fun download(url: String, dest: File, label: String): Boolean = withContext(Dispatchers.IO) {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000; conn.readTimeout = 600000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")
        val total = conn.contentLengthLong
        var downloaded = 0L
        val buf = ByteArray(65536)
        conn.inputStream.use { input -> FileOutputStream(dest).use { output ->
                var n: Int
                while (input.read(buf).also { n = it } != -1) { output.write(buf, 0, n); downloaded += n; if (total > 0) onProgress(label, downloaded.toFloat() / total) }
            } }
        dest.exists() && dest.length() > 0
    }

    private fun extractTarXz(tarFile: File, destDir: File) {
        destDir.mkdirs()
        val proc = ProcessBuilder("tar", "-xJf", tarFile.absolutePath, "-C", destDir.absolutePath)
            .redirectErrorStream(true).start()
        proc.waitFor()
    }

    companion object {
        fun selectJreVersion(mcVersionId: String): Int {
            val parts = mcVersionId.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
            if (major > 2) return 25        // 26.x → Java 25
            if (minor >= 25) return 25      // 1.25+ → Java 25
            if (minor >= 21) return 25      // 1.21+ → Java 25 (向前兼容)
            if (minor >= 17) return 21      // 1.17+ → Java 21
            if (minor in 13..16) return 17  // 1.13-1.16 → Java 17
            return 8                        // ≤1.12 → Java 8
        }
    }
}
