import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:litelauncher/screens/main_shell.dart';

void main() {
  testWidgets('MainShell renders 4 tabs', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: MainShell()));
    expect(find.text('首页'), findsWidgets);
    expect(find.text('版本'), findsWidgets);
  });
}
