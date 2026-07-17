import 'package:flutter/material.dart';
import '../core/theme/colors.dart';

class GlassPanel extends StatelessWidget {
  final Widget child;
  final EdgeInsets padding;
  const GlassPanel({super.key, required this.child, this.padding = const EdgeInsets.all(16)});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: AppColors.surface.withValues(alpha: 0.8),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.surfaceLight, width: 0.5),
        boxShadow: const [BoxShadow(color: Color(0x1000E5FF), blurRadius: 8, offset: Offset(0, 2))],
      ),
      child: child,
    );
  }
}

class NeonButton extends StatelessWidget {
  final String text;
  final IconData? icon;
  final VoidCallback onTap;
  final bool primary;
  const NeonButton({super.key, required this.text, this.icon, required this.onTap, this.primary = true});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 14),
        decoration: BoxDecoration(
          gradient: primary
              ? const LinearGradient(colors: [AppColors.accentCyan, AppColors.accentPurple])
              : null,
          color: primary ? null : AppColors.surface,
          borderRadius: BorderRadius.circular(12),
          border: primary ? null : Border.all(color: AppColors.surfaceLight),
        ),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          if (icon != null) ...[Icon(icon, size: 20, color: primary ? AppColors.background : AppColors.accentCyan), const SizedBox(width: 8)],
          Text(text, style: TextStyle(fontWeight: FontWeight.w600, color: primary ? AppColors.background : AppColors.accentCyan)),
        ]),
      ),
    );
  }
}

class LoaderBadge extends StatelessWidget {
  final String label;
  final Color color;
  const LoaderBadge({super.key, required this.label, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(color: color.withValues(alpha: 0.2), borderRadius: BorderRadius.circular(4)),
      child: Text(label, style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: color)),
    );
  }
}
