package com.litelauncher.app.managers

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.litelauncher.app.bridge.DeviceInfo
import java.io.File

/**
 * 设备性能分级 — 三级 (低/中/高)
 */
class DeviceProfiler(private val context: Context) {

    fun profile(): DeviceInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        am.getMemoryInfo(memInfo)

        val totalRamMb = memInfo.totalMem / (1024 * 1024)
        val availRamMb = memInfo.availMem / (1024 * 1024)
        val cores = Runtime.getRuntime().availableProcessors()
        val cpuModel = getCpuModel()
        val gpuRenderer = getGpuRenderer()
        val tier = calculateTier(totalRamMb, cores, cpuModel)

        val isRooted = checkRoot()
        val hasKsu = checkKsu()

        return DeviceInfo(
            totalRamMb = totalRamMb.toInt(),
            availableRamMb = availRamMb.toInt(),
            cpuModel = cpuModel,
            coreCount = cores,
            gpuRenderer = gpuRenderer,
            deviceTier = tier,
            isRooted = isRooted,
            hasKsuModule = hasKsu
        )
    }

    private fun getCpuModel(): String {
        return try {
            File("/proc/cpuinfo").readLines()
                .firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                ?.substringAfter(": ") ?: Build.HARDWARE
        } catch (e: Exception) { Build.HARDWARE }
    }

    private fun getGpuRenderer(): String {
        // 从 EGL 查询获取 GPU 信息
        return try {
            val egl = javax.microedition.khronos.egl.EGLContext.getEGL() as? javax.microedition.khronos.egl.EGL10
            val display = egl?.eglGetDisplay(javax.microedition.khronos.egl.EGL10.EGL_DEFAULT_DISPLAY)
            egl?.eglInitialize(display, IntArray(2))
            egl?.eglQueryString(display, javax.microedition.khronos.egl.EGL10.EGL_RENDERER) ?: Build.HARDWARE
        } catch (e: Exception) { Build.HARDWARE }
    }

    private fun calculateTier(ramMb: Long, cores: Int, cpuModel: String): Int {
        if (ramMb >= 8192 && cores >= 8 && (cpuModel.contains("SM") || cpuModel.contains("Dimensity 9")))
            return 2 // 高
        if (ramMb >= 6144 && cores >= 8) return 1 // 中
        if (ramMb >= 4096 && cores >= 4) return 1 // 中
        return 0 // 低
    }

    private fun checkRoot(): Boolean {
        val paths = listOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su",
            "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su",
            "/system/sd/xbin/su", "/system/bin/failsafe/su", "/data/local/su"
        )
        return paths.any { File(it).exists() }
    }

    private fun checkKsu(): Boolean {
        return File("/data/adb/ksu/modules/litelauncher_helper").exists()
    }
}
