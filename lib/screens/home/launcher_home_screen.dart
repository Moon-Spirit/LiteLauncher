import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../widgets/glass_panel.dart';
import '../../services/launcher_service.dart';
import '../../services/launcher_api.g.dart';

class LauncherHomeScreen extends StatefulWidget {
  const LauncherHomeScreen({super.key});
  @override
  State<LauncherHomeScreen> createState() => _LauncherHomeScreenState();
}

class _LauncherHomeScreenState extends State<LauncherHomeScreen> {
  DeviceInfo? _deviceInfo;
  AccountInfo? _account;
  // ignore: unused_field
  List<VersionInfo> _versions = [];
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final results = await Future.wait([
        launcherService.getDeviceInfo(),
        launcherService.getCurrentAccount(),
        launcherService.getVersionList(),
      ]);
      setState(() {
        _deviceInfo = results[0] as DeviceInfo;
        _account = results[1] as AccountInfo?;
        _versions = (results[2] as List<VersionInfo>).where((v) => v.type == 'release').toList();
        _loading = false;
      });
    } catch (e) {
      setState(() { _error = e.toString(); _loading = false; });
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_loading) return const Center(child: CircularProgressIndicator(color: AppColors.accentCyan));
    if (_error != null) return Center(child: Text('错误: $_error', style: const TextStyle(color: AppColors.error)));

    return Scaffold(
      appBar: AppBar(title: const Text('LiteLauncher', style: TextStyle(color: AppColors.accentCyan, fontWeight: FontWeight.bold))),
      body: ListView(padding: const EdgeInsets.all(16), children: [
        // 设备信息面板
        GlassPanel(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('设备信息', style: TextStyle(color: AppColors.textSecondary, fontSize: 12)),
          const SizedBox(height: 8),
          _infoRow('运行内存', '${_deviceInfo?.totalRamMb ?? 0} MB'),
          _infoRow('CPU', '${_deviceInfo?.cpuModel ?? '未知'} (${_deviceInfo?.coreCount ?? 0} 核)'),
          _infoRow('GPU', _deviceInfo?.gpuRenderer ?? '未知'),
          _infoRow('设备分级', ['低', '中', '高'][_deviceInfo?.deviceTier ?? 0]),
          _infoRow('KSU 模块', _deviceInfo?.hasKsuModule == true ? '已安装 ✅' : '未安装'),
        ])),

        const SizedBox(height: 16),

        // 账户
        GlassPanel(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          const Text('账户', style: TextStyle(color: AppColors.textSecondary, fontSize: 12)),
          const SizedBox(height: 8),
          if (_account != null && (_account!.username.isNotEmpty))
            Row(children: [const Icon(Icons.person, color: AppColors.accentCyan, size: 18), const SizedBox(width: 8),
              Text(_account!.username, style: const TextStyle(fontWeight: FontWeight.w600)),
              const Spacer(), TextButton(onPressed: () => _showOfflineLogin(), child: const Text('切换'))])
          else
            ElevatedButton(onPressed: () => _showOfflineLogin(),
              style: ElevatedButton.styleFrom(backgroundColor: AppColors.accentCyan),
              child: const Text('离线登录', style: TextStyle(color: Colors.black))),
        ])),

        const SizedBox(height: 24),

        // 启动按钮
        NeonButton(text: '开始游戏', icon: Icons.play_arrow_rounded, onTap: _account != null ? () => _launch() : () {}),
      ]),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(padding: const EdgeInsets.symmetric(vertical: 2), child: Row(children: [
      Text('$label: ', style: const TextStyle(color: AppColors.textSecondary)),
      Text(value, style: const TextStyle(fontWeight: FontWeight.w600)),
    ]));
  }

  void _showOfflineLogin() {
    final ctrl = TextEditingController();
    showDialog(context: context, builder: (ctx) => AlertDialog(
      title: const Text('离线登录'),
      content: TextField(controller: ctrl, decoration: const InputDecoration(hintText: '输入玩家名'), autofocus: true),
      actions: [
        TextButton(onPressed: () async {
          final acc = await launcherService.loginOffline(ctrl.text);
          setState(() => _account = acc);
          if (ctx.mounted) Navigator.pop(ctx);
        }, child: const Text('登录')),
      ],
    ));
  }

  void _launch() async {
    // TODO: 完整启动流程 — 检查版本是否已下载，选择版本，构建 GameConfig，启动
    showDialog(context: context, builder: (_) => const AlertDialog(content: Text('即将启动...')));
  }
}
