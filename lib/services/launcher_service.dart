// Pigeon 生成代码的 Flutter 端封装
import 'package:litelauncher/services/launcher_api.g.dart';

class LauncherService {
  final LauncherHostApi _api = LauncherHostApi();

  Future<List<VersionInfo>> getVersionList() => _api.getVersionList();
  Future<VersionDetail> getVersionDetail(String id) => _api.getVersionDetail(id);
  Future<void> installVersion(String id) => _api.installVersion(id);

  Future<AuthProgress> startMicrosoftLogin() => _api.startMicrosoftLogin();
  Future<AccountInfo> loginOffline(String username) => _api.loginOffline(username);
  Future<void> logout() => _api.logout();
  Future<AccountInfo> getCurrentAccount() => _api.getCurrentAccount();

  Future<GameStatus> startGame(GameConfig config) => _api.startGame(config);
  Future<void> stopGame() => _api.stopGame();
  Future<GameStatus> getGameStatus() => _api.getGameStatus();
  Future<void> injectTouchEvent(double x, double y, int action, int pointerId) =>
      _api.injectTouchEvent(x, y, action, pointerId);

  Future<DeviceInfo> getDeviceInfo() => _api.getDeviceInfo();
}

final launcherService = LauncherService();
