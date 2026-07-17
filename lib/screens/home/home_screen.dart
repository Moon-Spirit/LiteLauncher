import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import '../versions/version_list_screen.dart';
import 'launcher_home_screen.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});
  @override
  Widget build(BuildContext context) => const LauncherHomeScreen();
}
class VersionsScreen extends StatelessWidget {
  const VersionsScreen({super.key});
  @override
  Widget build(BuildContext context) => const VersionListScreen();
}
class AccountScreen extends StatelessWidget {
  const AccountScreen({super.key});
  @override
  Widget build(BuildContext context) => const Center(child: Text('账户', style: TextStyle(color: AppColors.textSecondary)));
}
class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});
  @override
  Widget build(BuildContext context) => const Center(child: Text('设置', style: TextStyle(color: AppColors.textSecondary)));
}
