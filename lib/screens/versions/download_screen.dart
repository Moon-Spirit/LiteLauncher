import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../../services/launcher_service.dart';
import '../../widgets/glass_panel.dart';

class DownloadScreen extends StatefulWidget {
  final String versionId;
  final String versionUrl;
  const DownloadScreen({super.key, required this.versionId, required this.versionUrl});

  @override
  State<DownloadScreen> createState() => _DownloadScreenState();
}

class _DownloadScreenState extends State<DownloadScreen> {
  double _progress = 0;
  String _status = '准备下载...';
  bool _done = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _startDownload();
  }

  Future<void> _startDownload() async {
    try {
      setState(() => _status = '正在下载版本文件...');
      await launcherService.installVersion(widget.versionId);
      setState(() { _progress = 1.0; _status = '下载完成'; _done = true; });
    } catch (e) {
      setState(() { _error = e.toString(); _status = '下载失败'; });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(title: Text('安装 ${widget.versionId}', style: const TextStyle(color: AppColors.accentCyan))),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
            if (_error != null) ...[
              const Icon(Icons.error_outline, size: 64, color: AppColors.error),
              const SizedBox(height: 16),
              Text(_error!, textAlign: TextAlign.center, style: const TextStyle(color: AppColors.error)),
            ] else ...[
              SizedBox(width: 80, height: 80, child: CircularProgressIndicator(value: _progress > 0 ? _progress : null, color: AppColors.accentCyan, strokeWidth: 4)),
              const SizedBox(height: 24),
              Text(_status, style: const TextStyle(fontSize: 16, color: AppColors.textPrimary)),
              const SizedBox(height: 8),
              Text('${(_progress * 100).toStringAsFixed(0)}%', style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold, color: AppColors.accentCyan)),
            ],
            if (_done) ...[
              const SizedBox(height: 24),
              NeonButton(text: '完成', icon: Icons.check, onTap: () => Navigator.pop(context, true)),
            ],
          ]),
        ),
      ),
    );
  }
}
