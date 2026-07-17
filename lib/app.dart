import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'core/theme/theme.dart';
import 'screens/main_shell.dart';

class LiteLauncherApp extends StatelessWidget {
  const LiteLauncherApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LiteLauncher',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.dark,
      darkTheme: AppTheme.dark,
      themeMode: ThemeMode.dark,
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [Locale('zh', 'CN')],
      locale: const Locale('zh', 'CN'),
      home: const MainShell(),
    );
  }
}
