import 'package:flutter/material.dart';
import '../../core/theme/colors.dart';

class GameScreen extends StatefulWidget {
  const GameScreen({super.key});

  @override
  State<GameScreen> createState() => _GameScreenState();
}

class _GameScreenState extends State<GameScreen> {

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(children: [
        // TODO: Flutter Texture widget — 接收原生 GL 渲染
        // Texture(textureId: _textureId),
        const Center(child: Text('Game View', style: TextStyle(color: AppColors.textSecondary))),
        // 触控覆盖层
        Positioned.fill(child: _TouchOverlay()),
        // 返回按钮
        Positioned(top: 16, left: 16, child: IconButton(icon: const Icon(Icons.arrow_back), color: AppColors.textPrimary, onPressed: () => Navigator.pop(context))),
      ]),
    );
  }
}

class _TouchOverlay extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return IgnorePointer(
      child: Column(children: [
        const Spacer(),
        // 底部控制区
        Container(
          height: 200,
          decoration: BoxDecoration(gradient: LinearGradient(begin: Alignment.topCenter, end: Alignment.bottomCenter, colors: [Colors.transparent, Colors.black54])),
        ),
      ]),
    );
  }
}
