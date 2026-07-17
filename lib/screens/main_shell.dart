import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';
import 'home/home_screen.dart';

class MainShell extends StatefulWidget {
  const MainShell({super.key});

  @override
  State<MainShell> createState() => _MainShellState();
}

class _MainShellState extends State<MainShell> {
  int _index = 0;

  static final _screens = <Widget>[const HomeScreen(), const VersionsScreen(), const AccountScreen(), const SettingsScreen()];
  static const _labels = ['首页', '版本', '账户', '设置'];
  static const _icons = [Icons.home_rounded, Icons.inventory_2_rounded, Icons.person_rounded, Icons.settings_rounded];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(index: _index, children: _screens),
      bottomNavigationBar: Container(
        decoration: const BoxDecoration(border: Border(top: BorderSide(color: AppColors.surfaceLight, width: 0.5))),
        child: BottomNavigationBar(
          currentIndex: _index,
          onTap: (i) => setState(() => _index = i),
          type: BottomNavigationBarType.fixed,
          selectedFontSize: 11,
          unselectedFontSize: 11,
          items: List.generate(4, (i) => BottomNavigationBarItem(icon: Icon(_icons[i]), label: _labels[i])),
        ),
      ),
    );
  }
}
