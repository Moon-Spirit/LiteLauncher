import 'package:flutter/material.dart';
import 'colors.dart';

class AppTheme {
  static final dark = ThemeData(
    useMaterial3: true,
    brightness: Brightness.dark,
    scaffoldBackgroundColor: AppColors.background,
    colorScheme: const ColorScheme.dark(
      surface: AppColors.surface,
      primary: AppColors.accentCyan,
      secondary: AppColors.accentPurple,
      error: AppColors.error,
    ),
    appBarTheme: const AppBarTheme(
      backgroundColor: Colors.transparent,
      elevation: 0,
    ),
    cardTheme: CardThemeData(
      color: AppColors.surface,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
    ),
    bottomNavigationBarTheme: const BottomNavigationBarThemeData(
      backgroundColor: AppColors.surface,
      selectedItemColor: AppColors.accentCyan,
      unselectedItemColor: AppColors.textSecondary,
    ),
  );
}
