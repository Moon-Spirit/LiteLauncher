import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../services/launcher_service.dart';
import '../../services/launcher_api.g.dart';
import '../../widgets/glass_panel.dart';
import 'download_screen.dart';

class VersionListScreen extends StatefulWidget {
  const VersionListScreen({super.key});
  @override
  State<VersionListScreen> createState() => _VersionListScreenState();
}

class _VersionListScreenState extends State<VersionListScreen> {
  List<VersionInfo>? _versions;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final all = await launcherService.getVersionList();
      setState(() { _versions = all.where((v) => v.type == 'release').take(50).toList(); _loading = false; });
    } catch (e) {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('版本列表', style: TextStyle(color: AppColors.accentCyan))),
      body: _loading
          ? const Center(child: CircularProgressIndicator(color: AppColors.accentCyan))
          : _versions == null
              ? Center(child: ElevatedButton(onPressed: _load, child: const Text('重试')))
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: _versions!.length,
                  itemBuilder: (ctx, i) {
                    final v = _versions![i];
                    return Card(
                      margin: const EdgeInsets.only(bottom: 8),
                      child: ListTile(
                        leading: Icon(v.installed ? Icons.check_circle : Icons.cloud_download, color: v.installed ? AppColors.success : AppColors.textSecondary),
                        title: Text(v.id, style: const TextStyle(fontWeight: FontWeight.w600)),
                        subtitle: Text(v.releaseTime, style: const TextStyle(fontSize: 12, color: AppColors.textSecondary)),
                        trailing: const Icon(Icons.chevron_right, color: AppColors.textSecondary),
                        onTap: v.installed ? null : () {
                          Navigator.push(context, MaterialPageRoute(builder: (_) => VersionDetailScreen(version: v)));
                        },
                      ),
                    );
                  },
                ),
    );
  }
}

class VersionDetailScreen extends StatelessWidget {
  final VersionInfo version;
  const VersionDetailScreen({super.key, required this.version});

  void _install(BuildContext context) {
    Navigator.push(context, MaterialPageRoute(
      builder: (_) => DownloadScreen(versionId: version.id, versionUrl: version.url),
    ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(version.id, style: const TextStyle(color: AppColors.accentCyan))),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          GlassPanel(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            _row('版本', version.id),
            _row('类型', version.type),
            _row('发布时间', version.releaseTime),
          ])),
          const SizedBox(height: 24),
          NeonButton(text: '安装此版本', icon: Icons.download_rounded, onTap: () => _install(context)),
        ]),
      ),
    );
  }

  Widget _row(String label, String value) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 4),
    child: Row(children: [SizedBox(width: 80, child: Text(label, style: const TextStyle(color: AppColors.textSecondary))), Text(value, style: const TextStyle(fontWeight: FontWeight.w600))]),
  );
}
