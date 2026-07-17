package com.litelauncher.app.bridge

import com.litelauncher.app.managers.*
import android.content.Context

/**
 * Pigeon HostApi 实现 — 桥接 Flutter ↔ Kotlin Manager
 */
class LauncherHostApiImpl(
    context: Context
) : LauncherHostApi {
    private val versionManager = VersionManager()
    private val accountManager = AccountManager(context)
    private val deviceProfiler = DeviceProfiler(context)
    private val downloadManager = DownloadManager(context)
    private val gameLauncher = GameLauncher(context)
    private val modManager = ModManager(context)

    // ===== 版本 =====
    override suspend fun getVersionList(): List<VersionInfo> = versionManager.fetchVersions()
    override suspend fun getVersionDetail(versionId: String): VersionDetail = versionManager.getDetail(versionId)
    override suspend fun installVersion(versionId: String) = downloadManager.installVersion(versionId)

    // ===== 账户 =====
    override suspend fun startMicrosoftLogin(): AuthProgress = accountManager.startMSLogin()
    override suspend fun loginOffline(username: String): AccountInfo = accountManager.loginOffline(username)
    override suspend fun logout() = accountManager.logout()
    override suspend fun getCurrentAccount(): AccountInfo = accountManager.getCurrent()
    override suspend fun getAccounts(): List<AccountInfo> = accountManager.listAll()

    // ===== 游戏 =====
    override suspend fun startGame(config: GameConfig): GameStatus = gameLauncher.start(config)
    override suspend fun stopGame() = gameLauncher.stop()
    override suspend fun getGameStatus(): GameStatus = gameLauncher.getStatus()
    override suspend fun injectTouchEvent(x: Double, y: Double, action: Int, pointerId: Int) =
        gameLauncher.injectTouch(x, y, action, pointerId)

    // ===== 下载 =====
    override suspend fun cancelDownload(taskId: String) = downloadManager.cancel(taskId)

    // ===== 设备 =====
    override suspend fun getDeviceInfo(): DeviceInfo = deviceProfiler.profile()

    // ===== Mod =====
    override suspend fun getInstalledMods(gameDir: String, loaderType: String): List<ModInfo> =
        modManager.getInstalled(gameDir, loaderType)
    override suspend fun searchModrinth(query: String, mcVersion: String, loaderType: String): List<ModrinthProject> =
        modManager.searchModrinth(query, mcVersion, loaderType)
    override suspend fun installModrinthMod(projectId: String, versionId: String, gameDir: String) =
        modManager.installModrinth(projectId, versionId, gameDir)
    override suspend fun toggleMod(gameDir: String, fileName: String, enabled: Boolean) =
        modManager.toggle(gameDir, fileName, enabled)
    override suspend fun installModLoader(versionId: String, loaderType: String, loaderVersion: String) =
        modManager.installLoader(versionId, loaderType, loaderVersion)
}
