# Changelog

All notable changes to AI Mobile Studio will be documented in this file.

## [1.0.0] - 2026-09-27

### Added
- Complete initial release of AI Mobile Studio.
- Room database persistence for projects, tasks, conversations, tool calls, and checkpoints.
- Android Keystore AES-256 GCM credential encryption manager.
- Rootless Linux PRoot runtime environment manager with ARM64 CPU verification.
- JNI native process bridge skeleton (`process-bridge.c`).
- AgentAdapter interface and registry with 5 initial adapters:
  - Claude Code CLI
  - Antigravity Mobile CLI
  - DeepSeek Coder CLI (with R1 reasoning output)
  - OpenCode Assistant (offline capable)
  - Generic Custom CLI Agent
- Project workspace manager with 11 templates (React, Next.js, Vite, Node, HTML, Python, Android, C++, PHP, Linux).
- Interactive mobile code editor with syntax highlighting, line numbers, and AI actions.
- Full Linux terminal with PTY execution, command history, and quick chips.
- Local development server detection and live Android WebView preview.
- On-device Android build pipeline with APK inspection, SHA-256 calculation, and Package Installer launch.
- Git repository manager with visual diff viewer.
- Point-in-time snapshot checkpoint manager.
- 6-step interactive onboarding wizard.
