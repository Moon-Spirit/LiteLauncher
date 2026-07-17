import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';

class ModListScreen extends StatelessWidget {
  const ModListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Mod 管理', style: TextStyle(color: AppColors.accentCyan))),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: const [
          _ModCard(name: 'Sodium', version: '0.5.0', loader: 'fabric', enabled: true),
          _ModCard(name: 'JEI', version: '15.2.0', loader: 'forge', enabled: true),
          _ModCard(name: 'OptiFine', version: 'HD_U_G5', loader: 'optifine', enabled: false),
        ],
      ),
    );
  }
}

class _ModCard extends StatelessWidget {
  final String name, version, loader;
  final bool enabled;
  const _ModCard({required this.name, required this.version, required this.loader, required this.enabled});

  Color get loaderColor => switch (loader) {
    'fabric' => AppColors.fabric, 'quilt' => AppColors.quilt, 'forge' => AppColors.forge,
    'neoforge' => AppColors.neoforge, 'optifine' => AppColors.optifine, _ => AppColors.textSecondary,
  };

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        leading: Container(width: 4, decoration: BoxDecoration(color: loaderColor, borderRadius: BorderRadius.circular(2))),
        title: Text(name, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text('$loader · v$version', style: const TextStyle(fontSize: 12, color: AppColors.textSecondary)),
        trailing: Switch(value: enabled, onChanged: (_) {}),
      ),
    );
  }
}
