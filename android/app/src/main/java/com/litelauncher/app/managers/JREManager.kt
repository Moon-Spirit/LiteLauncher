package com.litelauncher.app.managers

import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * JRE 管理器 — 下载多个 Java 运行时 (8, 17, 21, 25)
 */
class JREManager(private val onProgress: (String, Float) -> Unit) {

    data class JREInfo(val majorVersion: Int, val url: String, val dir: String)

    private val jreList = listOf(
        JREInfo(8,  "https://api.adoptium.net/v3/binary/latest/8/ga/linux/aarch64/jre/hotspot/normal/adoptium", "jre-8"),
        JREInfo(17, "https://api.adoptium.net/v3/binary/latest/17/ga/linux/aarch64/jre/hotspot/normal/adoptium", "jre-17"),
        JREInfo(21, "https://api.adoptium.net/v3/binary/latest/21/ga/linux/aarch64/jre/hotspot/normal/adoptium", "jre-21"),
        JREInfo(25, "https://api.adoptium.net/v3/binary/latest/25/ga/linux/aarch64/jre/hotspot/normal/adoptium", "jre-25"),
    )

    /**
     * 根据需要下载 JRE
     * @param requiredVersion MC 版本对应的 majorVersion (8/17/21/25)
     * @param dataDir 应用数据目录
     */
    suspend fun ensureJRE(requiredVersion: Int, dataDir: String): String = withContext(Dispatchers.IO) {
        val jre = jreList.firstOrNull { it.majorVersion == requiredVersion }
            ?: throw IllegalArgumentException("不支持的 Java 版本: $requiredVersion")

        val jreDir = File(dataDir, "jre/${jre.dir}")
        val javaBin = File(jreDir, "bin/java")

        if (javaBin.exists() && javaBin.canExecute()) {
            onProgress("jre-$requiredVersion", 1.0f)
            return@withContext jreDir.absolutePath
        }

        // 下载 tar.gz
        val tarFile = File(dataDir, "jre/${jre.dir}.tar.gz")
        tarFile.parentFile?.mkdirs()

        val conn = URL(jre.url).openConnection() as HttpURLConnection
        conn.connectTimeout = 15000; conn.readTimeout = 600000
        conn.setRequestProperty("User-Agent", "LiteLauncher/1.0")
        val total = conn.contentLengthLong

        var downloaded = 0L
        val buf = ByteArray(65536)
        conn.inputStream.use { input -> FileOutputStream(tarFile).use { output ->
                var n: Int
                while (input.read(buf).also { n = it } != -1) { output.write(buf, 0, n); downloaded += n; if (total > 0) onProgress("jre-$requiredVersion", downloaded.toFloat() / total) }
            } }

        // 解压
        extractTarGz(tarFile, jreDir)
        tarFile.delete()

        javaBin.setExecutable(true)
        jreDir.absolutePath
    }

    private fun extractTarGz(tarFile: File, destDir: File) {
        destDir.mkdirs()
        // 使用系统 tar 解压 (Android 上有 busybox tar)
        val proc = ProcessBuilder("tar", "-xzf", tarFile.absolutePath, "-C", destDir.absolutePath)
            .redirectErrorStream(true)
            .start()
        proc.waitFor()
    }

    companion object {
        fun selectJreVersion(mcVersionId: String): Int {
            val parts = mcVersionId.split(".")
            val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0

            // New format: YY.D.H
            if (major > 2) return 25 // 26.x → Java 25
            // Old format: 1.x.x
            if (minor >= 21) return 25
            if (minor >= 17) return 21
            if (minor in 13..16) return 17
            return 8
        }
    }
}
