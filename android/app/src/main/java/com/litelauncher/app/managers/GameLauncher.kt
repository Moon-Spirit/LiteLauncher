package com.litelauncher.app.managers

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.view.Surface
import com.litelauncher.app.bridge.GameConfig
import com.litelauncher.app.bridge.GameStatus
import com.litelauncher.runtime.IMCRuntimeService
import java.io.File

/**
 * 游戏启动器 — 构建启动参数 + 绑定 MCRuntimeService + 启动游戏
 */
class GameLauncher(private val context: Context) {
    private var service: IMCRuntimeService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = IMCRuntimeService.Stub.asInterface(binder)
            bound = true
        }
        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    suspend fun start(config: GameConfig): GameStatus {
        bindService()

        val (jvmArgs, mcArgs) = buildArgs(config)

        val result = service?.startGame(
            config.gameDir,
            config.javaHome,
            buildClasspath(config),
            getMainClass(config.loaderType),
            jvmArgs.toTypedArray(),
            mcArgs.toTypedArray()
        ) ?: return GameStatus(state = 5, fps = 0, usedRamMb = 0, crashReport = "服务绑定失败")

        if (result < 0) {
            return GameStatus(state = 5, fps = 0, usedRamMb = 0, crashReport = "启动失败, 返回值=$result")
        }

        return GameStatus(state = 4, fps = 0, usedRamMb = 0, crashReport = null)
    }

    suspend fun stop() {
        service?.stopGame()
        unbindService()
    }

    suspend fun getStatus(): GameStatus {
        val state = service?.state ?: 0
        return GameStatus(state = state, fps = 0, usedRamMb = 0, crashReport = service?.crashReport)
    }

    suspend fun injectTouch(x: Double, y: Double, action: Int, pointerId: Int) {
        // 编码: [type, action, x*10000, y*10000, pressure*10000, pointerId]
        val events = intArrayOf(
            if (action == 0) 0 else if (action == 1) 2 else 1, // type: DOWN=0, MOVE=1, UP=2
            action,
            (x * 10000).toInt(),
            (y * 10000).toInt(),
            10000, // pressure = 1.0
            pointerId
        )
        service?.injectInput(events)
    }

    fun setSurface(surface: Surface) {
        service?.setSurface(surface)
    }

    private fun bindService() {
        val intent = Intent().apply {
            component = ComponentName("com.litelauncher.app", "com.litelauncher.runtime.MCRuntimeService")
        }
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindService() {
        if (bound) context.unbindService(connection)
    }

    private fun buildArgs(config: GameConfig): Pair<List<String>, List<String>> {
        val jvmArgs = mutableListOf(
            "-Xms${if (config.maxRamMb > 4096) config.maxRamMb/2 else config.maxRamMb}M",
            "-Xmx${config.maxRamMb}M",
            "-XX:+UseG1GC",
            "-XX:MaxGCPauseMillis=200",
            "-XX:MaxTenuringThreshold=1",
            "-XX:+AlwaysPreTouch",
            "-XX:+DisableExplicitGC",
            "-Djava.library.path=${config.gameDir}/natives",
            "-Dfml.ignoreInvalidMinecraftCertificates=true",
            "-Dfml.ignorePatchDiscrepancies=true"
        )
        jvmArgs.addAll(config.extraJvmArgs)

        val mcArgs = listOf(
            "--username", "Player",
            "--version", config.versionId,
            "--gameDir", config.gameDir,
            "--assetsDir", "${config.gameDir}/assets",
            "--assetIndex", config.versionId,
            "--uuid", "00000000-0000-0000-0000-000000000000",
            "--accessToken", "0",
            "--userType", "mojang",
            "--versionType", "release"
        )

        return Pair(jvmArgs, mcArgs)
    }

    private fun buildClasspath(config: GameConfig): String {
        val libsDir = File(config.gameDir, "libraries")
        val jars = mutableListOf<String>()

        // Minecraft client jar
        jars.add("${config.gameDir}/versions/${config.versionId}/${config.versionId}.jar")

        // Libraries
        if (libsDir.exists()) {
            libsDir.walkTopDown().filter { it.extension == "jar" }.forEach { jars.add(it.absolutePath) }
        }

        return jars.joinToString(":")
    }

    private fun getMainClass(loaderType: String): String {
        return when (loaderType.lowercase()) {
            "fabric" -> "net.fabricmc.loader.impl.launch.knot.KnotClient"
            "quilt" -> "org.quiltmc.loader.impl.launch.knot.KnotClient"
            "forge" -> "cpw.mods.modlauncher.Launcher"
            "neoforge" -> "net.neoforged.fancymodloader.loader.Launcher"
            "optifine" -> "net.minecraft.client.main.Main"
            else -> "net.minecraft.client.main.Main"
        }
    }
}
