# Contributing to Unreel

Thank you for contributing to Unreel. This document outlines our guidelines for maintaining high code quality, test-driven development, and clean architecture.

---

## 🎯 Development Principles

1. **Test-Driven Development (TDD)**
   - Always write a failing test first.
   - Write the minimum viable code to make the test pass.
   - Refactor for clarity and performance while preserving test green state.
   - Add property-based tests for algorithmic calculations (e.g. metrics, rule evaluation, day boundaries).

2. **Clean Architecture (Hexagonal / Ports & Adapters)**
   - Business logic in `shared:domain` and `shared:core-model` MUST remain pure Kotlin.
   - Never import `android.*` or Apple platform APIs into domain modules.
   - External dependencies (Clock, Storage, Device APIs) must be provided via interfaces in `domain/port`.

3. **Privacy First**
   - Never inspect, log, or persist user content, video titles, captions, or messages.
   - Log only timestamp, dwell time, and tracked package ID.

---

## 🌿 Git Workflow

### Branch Naming Convention
- `feature/<ticket-or-id>-<short-description>` (e.g. `feature/F-01-core-abstractions`)
- `fix/<ticket-or-id>-<short-description>` (e.g. `fix/F-02-midnight-boundary-calculation`)
- `docs/<short-description>` (e.g. `docs/adr-002-device-activity`)

### Conventional Commits
All commits must follow the Conventional Commits specification:

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

**Types:**
- `feat`: A new feature
- `fix`: A bug fix
- `test`: Adding or correcting tests
- `refactor`: Code change that neither fixes a bug nor adds a feature
- `docs`: Documentation updates
- `ci`: CI configuration and workflow files
- `chore`: Build tooling, dependency version bumps, or housekeeping

---

## 📋 Pull Request Checklist

Before submitting a Pull Request:
1. Ensure all shared JVM unit tests pass:
   ```bash
   ./gradlew :shared:core-model:jvmTest :shared:core-time:jvmTest :shared:domain:jvmTest :shared:testing:jvmTest
   ```
2. Ensure Android host tests and unit tests pass:
   ```bash
   ./gradlew :shared:data:testAndroidHostTest :shared:presentation:testAndroidHostTest :androidApp:testDebugUnitTest
   ```
3. Verify Android Lint and build:
   ```bash
   ./gradlew :androidApp:lintDebug :androidApp:assembleDebug
   ```
4. Ensure iOS tests pass (on macOS):
   ```bash
   ./gradlew iosSimulatorArm64Test
   ```
5. Keep changesets small and reviewable (< ~400 lines changed per PR).
6. Fill out all sections in the pull request template.

### Local Git Hooks (Recommended)
To automatically validate your commit messages locally against the Conventional Commits specification before committing:
```bash
git config core.hooksPath .githooks
```
