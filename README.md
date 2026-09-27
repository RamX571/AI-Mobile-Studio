# AI Mobile Studio

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![Tests](https://img.shields.io/badge/tests-18%20passed-success.svg)](#)
[![Latest Release](https://img.shields.io/badge/release-v1.0.0-blue.svg)](#)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](#)
[![Android](https://img.shields.io/badge/Android-API%2024%2B%20(Android%207.0%2B)-green.svg)](#)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](#)
[![Target ABI](https://img.shields.io/badge/ABI-ARM64%20(aarch64)-orange.svg)](#)

> **On-Device AI Software Development Environment for Android**  
> An original, production-oriented mobile IDE and agent workspace featuring a rootless Linux runtime, multi-agent adapter architecture, real-time code editor, live WebView preview, and native on-device Android compilation pipeline.

---

## ⚠️ Security Notice & Isolation Disclosure

> **PRoot / Userspace Execution Boundary**:  
> The Linux environment provided inside AI Mobile Studio operates as a rootless userspace compatibility layer using `ptrace` and system call interception inside application-private storage (`/data/user/0/.../app-workspace/`).  
> **It is NOT a hardware virtualization machine (VM) or hardened isolation boundary.**  
> Users should only execute code, dependencies, and npm/pip packages that they trust. The application provides an explicit **Permission Model** (*Safe Mode*, *Ask Before Sensitive*, and *Trusted Project*) to prevent unauthorized script execution or file modifications.

---

## ✨ Features

- **Project Workspace Manager**: Create and manage projects across 11 templates: React, Next.js, Vite, Node.js (Express), Static HTML/CSS/JS, Python 3, Android (Kotlin), Android (Java), C/C++, PHP, and Generic Linux.
- **Rootless Linux Runtime**: Native ARM64 userspace execution without requiring device root. Pre-configured with Node.js, npm, Python, Git, OpenJDK 17, and Gradle toolchains.
- **Pluggable Agent Adapter Layer (`AgentAdapter`)**:
  - **Claude Code CLI** (Anthropic)
  - **Antigravity CLI** (Gemini/Cloud orchestrator)
  - **DeepSeek Coder CLI** (R1 Reasoner with structured thinking traces)
  - **OpenCode Assistant** (Offline-capable local weights)
  - **Generic Custom CLI Agent**
- **Hardware-Backed Credential Security**: Android Keystore AES-256 GCM encryption. API keys are encrypted at rest and never written to plain text project files, logs, or backups.
- **Interactive Mobile Code Editor**: Syntax highlighting, line numbers, dirty indicator, save shortcut, and AI quick actions (*Explain*, *Fix*, *Refactor*, *Show Diff*).
- **Embedded Real Terminal**: Multiple session support, PTY execution, command history, ANSI colors, Ctrl+C interrupt, clear, and shortcut chips (`npm run dev`, `ls -lah`, `git status`).
- **Live Web Preview**: Localhost dev server detection (ports `3000`, `4173`, `5173`, `8000`, `8080`), embedded Android WebView, reload, and external browser launch.
- **On-Device Android Build System**: Executes Gradle and aapt2 on phone, inspects APK metadata, generates SHA-256 checksums, and launches the Android Package Installer via `FileProvider`.
- **Git & Checkpoints**: Built-in Git commands (`init`, `status`, `add`, `commit`, `log`, `branch`), visual line-by-line diff engine, and point-in-time snapshot snapshots.
- **Persistent Background Development Service**: Foreground Service with ongoing notification keeps local web servers and AI tasks running across app switches.
- **6-Step Onboarding Wizard**: Checks hardware compatibility, Linux rootfs, toolchains, agents, Keystore security, and initial project scaffolding.

---

## 🏗️ Architecture

```
AI Mobile Studio
├── Android Host (Jetpack Compose + Material 3)
│   ├── Navigation Bar & Screens (Projects, Workspace, Terminal, Agents, Health, Settings)
│   ├── SecureCredentialManager (Android Keystore AES-256-GCM)
│   ├── WorkspaceManager (App-Private /app-workspace/ Storage & Templates)
│   └── RuntimeForegroundService (Background Task Engine)
│
├── Agent Layer (AgentAdapter Abstraction)
│   ├── AgentRegistry (Dynamic Discovery)
│   ├── ClaudeCodeAgentAdapter
│   ├── AntigravityAgentAdapter
│   ├── DeepSeekAgentAdapter
│   ├── OpenCodeAgentAdapter
│   └── GenericCliAgentAdapter
│
├── Linux Execution Layer (PRoot Rootless Userland)
│   ├── LinuxRuntimeManager (Process Supervisor & Streaming PTY)
│   ├── NativeProcessBridge (JNI / C Syscall Bridge)
│   ├── Toolchains (Node, Python, Git, OpenJDK, Gradle, aapt2)
│   └── PortManager (Localhost Server Discovery)
│
└── Build & Release Pipeline
    ├── AndroidBuildManager (Compilation, Package Installer Integration)
    ├── GitRepositoryManager (Diff & Version Control)
    └── CheckpointManager (Snapshot & Rollback)
```

---

## 🚀 Quick Start Workflow

1. **Launch & Verify**: Complete the 6-step onboarding wizard to verify ARM64 CPU compatibility and initialize the PRoot rootfs.
2. **Create Project**: Tap `+ New Project`, select `React` (or your preferred template), and choose a permission mode.
3. **Prompt the Agent**: In the **Chat** tab, ask:
   ```
   "Create an expense tracker with a live dashboard and currency filters."
   ```
4. **Inspect Tool Calls**: Watch the agent read files, modify source code, execute tests, and prompt for sensitive approvals.
5. **Preview Web App**: Switch to the **Preview** tab to interact with your live app running on `http://127.0.0.1:3000`.
6. **Build APK**: Switch to the **Build** tab, tap `Assemble APK`, inspect the package metadata and SHA-256, and tap `Install via Package Installer`.

---

## 📦 System Requirements

- **Processor**: 64-bit ARM (`arm64-v8a` / `aarch64`)
- **Android Version**: Android 7.0 (API level 24) or higher (Target: Android 15 / API 36)
- **RAM**: Minimum 3 GB (Recommended: 6 GB+ for on-device compilation)
- **Storage**: Minimum 500 MB free internal storage

---

## 🛠️ Build from Source

```bash
# Clone repository
git clone https://github.com/your-org/AI-Mobile-Studio.git
cd AI-Mobile-Studio

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Build release APK
./gradlew assembleRelease
```

---

## 📄 License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
