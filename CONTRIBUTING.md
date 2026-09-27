# Contributing to AI Mobile Studio

Thank you for contributing to AI Mobile Studio!

## Development Guidelines

1. **Architecture Separation**:
   - Keep UI code in `ui/screens/` and `ui/components/`.
   - Never couple UI directly to individual agent CLI binaries; always use `AgentAdapter` and `AgentRegistry`.
   - Do NOT store API keys in plain text; use `SecureCredentialManager`.

2. **Code Style**:
   - Use Kotlin and Jetpack Compose (Material 3).
   - Follow standard Android coding conventions.
   - Run `./gradlew test` and `./gradlew lint` before submitting PRs.

3. **Submitting Changes**:
   - Create a feature branch (`git checkout -b feature/agent-extension`).
   - Commit changes with descriptive messages.
   - Ensure all Robolectric and unit tests pass.
