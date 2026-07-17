# AGENTS.md — LiteLauncher

## Project
- **Name**: LiteLauncher — Minecraft JE Launcher for Android
- **Framework**: Flutter (BLoC) + Kotlin native module + C/C++ engine
- **License**: GPLv3
- **Organization**: Moon-Spirit

## Architecture
```
ksu_module/    → KSU root module (Phase -1, kernel-level process protection)
lib/           → Flutter/Dart (Wave 3, UI layer)
android/       → Kotlin + C/C++ (Waves 1+2, engine + backend)
lwjgl2/        → LWJGL2 Android fork (Wave 5, MC ≤1.12)
lwjgl3/        → LWJGL3 Android fork (Wave 5, MC ≥1.13)
```

## Conventions
- Commit prefix: `p-1/` (KSU), `w0/`–`w6/` (waves)
- Tag checkpoints: `v0.1.0-alpha` – `v1.0.0`
- Category+skills delegation per task plan
- TDD: RED → GREEN → SURFACE for all behavior changes
