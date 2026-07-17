// Pigeon API 定义 — LiteLauncher Flutter ↔ Kotlin 通信契约
// 运行: dart run pigeon --input lib/services/launcher_api.dart

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/services/launcher_api.g.dart',
  kotlinOut: 'android/app/src/main/java/com/litelauncher/app/bridge/LauncherApi.kt',
  kotlinPackageName: 'com.litelauncher.app.bridge',
))

// ===== 版本管理 =====

class VersionInfo {
  final String id;
  final String type;
  final String url;
  final String releaseTime;
  final bool installed;
  final String? loaderName;
}

class VersionDetail {
  final String id;
  final String type;
  final String mainClass;
  final String assetsIndex;
  final int javaVersion;
  final String releaseTime;
  final int size;
}

// ===== 账户 =====

enum AccountType { microsoft, offline, yggdrasil }

class AccountInfo {
  final String id;
  final String username;
  final String uuid;
  final AccountType type;
  final String? skinUrl;
  final String? authServer;
}

class AuthProgress {
  final String userCode;
  final String verificationUri;
  final String status; // waiting, success, error
  final String? error;
}

// ===== 游戏 =====

class GameConfig {
  final String versionId;
  final String gameDir;
  final String javaHome;
  final int maxRamMb;
  final int renderMode; // 1=GL4ES, 2=MobileGlues, 3=Zink
  final String loaderType; // vanilla, fabric, forge, neoforge, quilt, optifine
  final List<String> extraJvmArgs;
}

class GameStatus {
  final int state; // 0=idle, 1=loading, 3=ready, 4=running, 5=crashed, 6=stopped
  final int fps;
  final int usedRamMb;
  final String? crashReport;
}

// ===== 下载 =====

class DownloadProgress {
  final String taskId;
  final String name;
  final double progress; // 0.0-1.0
  final int downloadedBytes;
  final int totalBytes;
  final int speedBytesPerSec;
  final String status; // downloading, paused, completed, error
}

// ===== 设备信息 =====

class DeviceInfo {
  final int totalRamMb;
  final int availableRamMb;
  final String cpuModel;
  final int coreCount;
  final String gpuRenderer;
  final int deviceTier; // 0=低, 1=中, 2=高
  final bool isRooted;
  final bool hasKsuModule;
}

// ===== Mod =====

class ModInfo {
  final String fileName;
  final String name;
  final String version;
  final String loaderType;
  final bool enabled;
  final int size;
}

class ModrinthProject {
  final String id;
  final String slug;
  final String title;
  final String description;
  final String iconUrl;
  final int downloads;
  final List<String> categories;
}

// ===== API 接口 =====

@HostApi()
abstract class LauncherHostApi {
  // 版本
  @async List<VersionInfo> getVersionList();
  @async VersionDetail getVersionDetail(String versionId);
  @async void installVersion(String versionId);

  // 账户
  @async AuthProgress startMicrosoftLogin();
  @async AccountInfo loginOffline(String username);
  @async void logout();
  @async AccountInfo getCurrentAccount();
  @async List<AccountInfo> getAccounts();

  // 游戏
  @async GameStatus startGame(GameConfig config);
  @async void stopGame();
  @async GameStatus getGameStatus();
  @async void injectTouchEvent(double x, double y, int action, int pointerId);

  // 下载
  @async void cancelDownload(String taskId);

  // 设备
  @async DeviceInfo getDeviceInfo();

  // Mod
  @async List<ModInfo> getInstalledMods(String gameDir, String loaderType);
  @async List<ModrinthProject> searchModrinth(String query, String mcVersion, String loaderType);
  @async void installModrinthMod(String projectId, String versionId, String gameDir);
  @async void toggleMod(String gameDir, String fileName, bool enabled);
  @async void installModLoader(String versionId, String loaderType, String loaderVersion);
}

// Flutter → Kotlin 的流式回调
@FlutterApi()
abstract class LauncherFlutterApi {
  void onDownloadProgress(DownloadProgress progress);
  void onAuthProgress(AuthProgress progress);
  void onGameStatusChange(GameStatus status);
  void onLog(String line);
}
