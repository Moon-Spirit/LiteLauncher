// Pigeon API definition
// Run: dart run pigeon --input lib/services/launcher_api.dart

import 'package:pigeon/pigeon.dart';

@ConfigurePigeon(PigeonOptions(
  dartOut: 'lib/services/launcher_api.g.dart',
  kotlinOut: 'android/app/src/main/java/com/litelauncher/app/bridge/LauncherApi.kt',
))

class VersionInfo {
  VersionInfo({required this.id, required this.type, required this.url, required this.releaseTime, required this.installed, this.loaderName});
  final String id;
  final String type;
  final String url;
  final String releaseTime;
  final bool installed;
  final String? loaderName;
}

class VersionDetail {
  VersionDetail({required this.id, required this.type, required this.mainClass, required this.assetsIndex, required this.javaVersion, required this.releaseTime, required this.size});
  final String id;
  final String type;
  final String mainClass;
  final String assetsIndex;
  final int javaVersion;
  final String releaseTime;
  final int size;
}

enum AccountType { microsoft, offline, yggdrasil }

class AccountInfo {
  AccountInfo({required this.id, required this.username, required this.uuid, required this.type, this.skinUrl, this.authServer});
  final String id;
  final String username;
  final String uuid;
  final AccountType type;
  final String? skinUrl;
  final String? authServer;
}

class AuthProgress {
  AuthProgress({required this.userCode, required this.verificationUri, required this.status, this.error});
  final String userCode;
  final String verificationUri;
  final String status;
  final String? error;
}

class GameConfig {
  GameConfig({required this.versionId, required this.gameDir, required this.javaHome, required this.maxRamMb, required this.renderMode, required this.loaderType, required this.extraJvmArgs});
  final String versionId;
  final String gameDir;
  final String javaHome;
  final int maxRamMb;
  final int renderMode;
  final String loaderType;
  final List<String> extraJvmArgs;
}

class GameStatus {
  GameStatus({required this.state, required this.fps, required this.usedRamMb, this.crashReport});
  final int state;
  final int fps;
  final int usedRamMb;
  final String? crashReport;
}

class DownloadProgress {
  DownloadProgress({required this.taskId, required this.name, required this.progress, required this.downloadedBytes, required this.totalBytes, required this.speedBytesPerSec, required this.status});
  final String taskId;
  final String name;
  final double progress;
  final int downloadedBytes;
  final int totalBytes;
  final int speedBytesPerSec;
  final String status;
}

class DeviceInfo {
  DeviceInfo({required this.totalRamMb, required this.availableRamMb, required this.cpuModel, required this.coreCount, required this.gpuRenderer, required this.deviceTier, required this.isRooted, required this.hasKsuModule});
  final int totalRamMb;
  final int availableRamMb;
  final String cpuModel;
  final int coreCount;
  final String gpuRenderer;
  final int deviceTier;
  final bool isRooted;
  final bool hasKsuModule;
}

class ModInfo {
  ModInfo({required this.fileName, required this.name, required this.version, required this.loaderType, required this.enabled, required this.size});
  final String fileName;
  final String name;
  final String version;
  final String loaderType;
  final bool enabled;
  final int size;
}

class ModrinthProject {
  ModrinthProject({required this.id, required this.slug, required this.title, required this.description, required this.iconUrl, required this.downloads, required this.categories});
  final String id;
  final String slug;
  final String title;
  final String description;
  final String iconUrl;
  final int downloads;
  final List<String> categories;
}

@HostApi()
abstract class LauncherHostApi {
  @async List<VersionInfo> getVersionList();
  @async VersionDetail getVersionDetail(String versionId);
  @async void installVersion(String versionId);
  @async AuthProgress startMicrosoftLogin();
  @async AccountInfo loginOffline(String username);
  @async void logout();
  @async AccountInfo getCurrentAccount();
  @async List<AccountInfo> getAccounts();
  @async GameStatus startGame(GameConfig config);
  @async void stopGame();
  @async GameStatus getGameStatus();
  @async void injectTouchEvent(double x, double y, int action, int pointerId);
  @async void cancelDownload(String taskId);
  @async DeviceInfo getDeviceInfo();
  @async List<ModInfo> getInstalledMods(String gameDir, String loaderType);
  @async List<ModrinthProject> searchModrinth(String query, String mcVersion, String loaderType);
  @async void installModrinthMod(String projectId, String versionId, String gameDir);
  @async void toggleMod(String gameDir, String fileName, bool enabled);
  @async void installModLoader(String versionId, String loaderType, String loaderVersion);
}

@FlutterApi()
abstract class LauncherFlutterApi {
  void onDownloadProgress(DownloadProgress progress);
  void onAuthProgress(AuthProgress progress);
  void onGameStatusChange(GameStatus status);
  void onLog(String line);
}
