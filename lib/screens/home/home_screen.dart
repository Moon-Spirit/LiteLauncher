import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('LiteLauncher', style: TextStyle(color: AppColors.accentCyan, fontWeight: FontWeight.bold))),
      body: const Center(child: Text('Home', style: TextStyle(color: AppColors.textSecondary))),
    );
  }
}

class VersionsScreen extends StatelessWidget {
  const VersionsScreen({super.key});
  @override
  Widget build(BuildContext context) => const Center(child: Text('Versions', style: TextStyle(color: AppColors.textSecondary)));
}

class AccountScreen extends StatelessWidget {
  const AccountScreen({super.key});
  @override
  Widget build(BuildContext context) => const Center(child: Text('Account', style: TextStyle(color: AppColors.textSecondary)));
}

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});
  @override
  Widget build(BuildContext context) => const Center(child: Text('Settings', style: TextStyle(color: AppColors.textSecondary)));
}
