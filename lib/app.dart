import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

class LiteLauncherApp extends StatelessWidget {
  const LiteLauncherApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'LiteLauncher',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0D1117),
        colorSchemeSeed: const Color(0xFF00E5FF),
      ),
      darkTheme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        scaffoldBackgroundColor: const Color(0xFF0D1117),
        colorSchemeSeed: const Color(0xFF00E5FF),
      ),
      themeMode: ThemeMode.dark,
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('zh', 'CN'),
        Locale('en', 'US'),
      ],
      locale: const Locale('zh', 'CN'),
      home: const Scaffold(
        body: Center(child: Text('LiteLauncher')),
      ),
    );
  }
}
