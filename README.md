# LiteLauncher

> Minecraft Java Edition Launcher for Android — Built with Flutter + KSU Root + LWJGL3 + MobileGlues

[![License: GPLv3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

LiteLauncher is a Minecraft: Java Edition mobile launcher for Android. It merges the best parts of PojavLauncher and Boat Launcher into a unified engine, with LWJGL3 port and KSU kernel module as core differentiators.

**Target**: 200+ mod modpacks on phones with 8-12GB RAM + KSU root + Scene8G swap.

## Features

- **All MC versions** — 1.7.10 to latest (26.x+)
- **5 mod loaders** — Fabric, Quilt, Forge, NeoForge, OptiFine — all versions
- **LWJGL3 + MobileGlues** — Desktop OpenGL 3.2 core profile on Android GLES 3.2
- **3-tier renderer dispatch** — GL4ES (≤1.12) → MobileGlues (1.13+) → Zink/Vulkan (premium)
- **4 JRE runtimes** — Java 8, 17, 21, 25 bundled
- **KSU kernel module** — oom_score_adj=-1000, CPU/GPU/IO tuning, Qualcomm + MediaTek
- **Microsoft OAuth + offline + authlib-injector** — full auth stack
- **Modrinth + CurseForge** — in-app mod discovery and download
- **BMCLAPI mirror** — fast downloads for Chinese users
- **Smart crash diagnosis** — Chinese error messages with repair suggestions
- **Customizable touch controls** — virtual joystick + draggable buttons

## Architecture

```
Flutter UI (BLoC) → Pigeon Channels → Kotlin Backend
                                       ↓ AIDL/Binder IPC
                                MC Runtime Service (:mc_runtime)
                                       ↓ JLI_Launch
                                Embedded JVM + Minecraft + Mod Loaders
                                       ↓ LWJGL2/3 + MobileGlues/Zink/GL4ES
                                KSU Module (oom=-1000, kernel tuning)
```

## Quick Start

*Coming soon — development in progress.*

## License

GPLv3 — see [LICENSE](LICENSE).

Based on:
- [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher) (LGPLv3)
- [Boat Launcher](https://github.com/AOF-Dev/Boat) (GPLv2)
- [MobileGlues](https://github.com/MobileGL-Dev/MobileGlues-release) (LGPL-2.1)

---

**Moon-Spirit** · [GitHub](https://github.com/Moon-Spirit)
