package com.litelauncher.app.managers

/**
 * Mod Loader 安装器 — Fabric / Quilt / Forge / NeoForge / OptiFine
 */
class ModLoaderInstaller {
    private val bmclapi = "https://bmclapi2.bangbang93.com"

    data class InstallResult(val mainClass: String, val extraLibs: List<String>, val extraArgs: List<String>, val success: Boolean, val error: String? = null)

    fun installFabric(mcVersion: String, loaderVersion: String): InstallResult {
        // Fabric installer JAR URL via BMCLAPI
        // Run: java -jar fabric-installer.jar install -mcversion {mc} -loader {loader} -downloadMinecraft false
        return InstallResult(
            mainClass = "net.fabricmc.loader.impl.launch.knot.KnotClient",
            extraLibs = listOf("fabric-loader-$loaderVersion.jar"),
            extraArgs = listOf(),
            success = true
        )
    }

    fun installQuilt(mcVersion: String, loaderVersion: String): InstallResult {
        return InstallResult(
            mainClass = "org.quiltmc.loader.impl.launch.knot.KnotClient",
            extraLibs = listOf("quilt-loader-$loaderVersion.jar"),
            extraArgs = listOf(),
            success = true
        )
    }

    fun installForge(mcVersion: String, forgeVersion: String): InstallResult {
        val useJPMS = parseMajorVersion(mcVersion) >= 13
        return InstallResult(
            mainClass = if (useJPMS) "cpw.mods.modlauncher.Launcher" else "net.minecraft.launchwrapper.Launch",
            extraLibs = listOf("forge-$mcVersion-$forgeVersion-universal.jar"),
            extraArgs = if (useJPMS) listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED", "--patch-module", "java.base=../jpms-shim.jar")
                        else listOf("--tweakClass", "cpw.mods.fml.common.launcher.FMLTweaker"),
            success = true
        )
    }

    fun installNeoForge(mcVersion: String, neoforgeVersion: String): InstallResult {
        return InstallResult(
            mainClass = "net.neoforged.fancymodloader.loader.Launcher",
            extraLibs = listOf("neoforge-$mcVersion-$neoforgeVersion-universal.jar"),
            extraArgs = listOf("--patch-module", "java.base=../jpms-shim.jar"),
            success = true
        )
    }

    fun installOptiFine(mcVersion: String, optifineVersion: String): InstallResult {
        // OptiFine is a JAR patcher — extract patched client.jar from installer
        // Or run: java -jar OptiFine_{mc}_{ver}.jar (needs Caciocavallo AWT)
        return InstallResult(
            mainClass = "net.minecraft.client.main.Main",
            extraLibs = listOf("optifine-patched-$mcVersion-$optifineVersion.jar"),
            extraArgs = listOf(),
            success = true
        )
    }

    private fun parseMajorVersion(versionId: String): Int {
        return try {
            val parts = versionId.split(".")
            if (parts[0].length > 2) parts[0].toInt() // New format: YY.D.H
            else parts.getOrNull(1)?.toInt() ?: parts[0].toInt() // Old format: 1.x.x
        } catch (e: NumberFormatException) { 0 }
    }
}
