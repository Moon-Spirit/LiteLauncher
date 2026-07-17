import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';

class CrashScreen extends StatelessWidget {
  final String crashReport;
  final String diagnosis;
  final VoidCallback onRestart;
  final VoidCallback onRestartNoMods;

  const CrashScreen({
    super.key,
    required this.crashReport,
    required this.diagnosis,
    required this.onRestart,
    required this.onRestartNoMods,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            const Spacer(),
            const Icon(Icons.warning_amber_rounded, size: 64, color: AppColors.error),
            const SizedBox(height: 16),
            const Text('游戏崩溃', textAlign: TextAlign.center, style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: AppColors.error)),
            const SizedBox(height: 12),
            Text(diagnosis, textAlign: TextAlign.center, style: const TextStyle(color: AppColors.textSecondary)),
            const SizedBox(height: 24),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(color: AppColors.surface, borderRadius: BorderRadius.circular(8)),
              child: SelectableText(crashReport, style: const TextStyle(fontSize: 12, color: AppColors.textSecondary, fontFamily: 'monospace')),
            ),
            const SizedBox(height: 24),
            ElevatedButton(onPressed: onRestart, style: ElevatedButton.styleFrom(backgroundColor: AppColors.accentCyan, foregroundColor: AppColors.background), child: const Text('重新启动')),
            const SizedBox(height: 8),
            OutlinedButton(onPressed: onRestartNoMods, child: const Text('清除模组后启动')),
            const Spacer(),
          ]),
        ),
      ),
    );
  }
}
