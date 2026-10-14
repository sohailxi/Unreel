# Unreel

[![CI](https://github.com/sohailxi/Unreel/actions/workflows/ci.yml/badge.svg)](https://github.com/sohailxi/Unreel/actions/workflows/ci.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.20-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2026%2B-3DDC84.svg?logo=android)](https://developer.android.com)
[![iOS](https://img.shields.io/badge/iOS-17.0%2B-000000.svg?logo=apple)](https://developer.apple.com/ios/)
[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)]()

> **Know your scroll. Reclaim your time.**

Unreel is a high-performance cross-platform application (Kotlin Multiplatform + Compose / SwiftUI) that tracks and limits short-video (reels/shorts) scrolling across supported platforms.

---

## 🏛 Architecture

Unreel is built adhering to **Clean Architecture** with **Unidirectional Data Flow (MVI)** and strict **Ports & Adapters (Hexagonal Architecture)**.

```
unreel/
├── build-logic/convention/       # Gradle convention plugins & version catalogs
├── shared/
│   ├── core-model/               # Pure Kotlin entities, value classes & domain rules
│   ├── core-time/                # Clock port, DayBoundary & timezone logic
│   ├── domain/                   # Ports (EventSources, Enforcers), Rule & Metrics engines
│   ├── data/                     # SQLDelight, repositories, local persistence
│   ├── presentation/             # ViewModels exposing StateFlow
│   └── testing/                  # FakeClock, FakeEventSources & test fixtures
├── androidApp/                   # Jetpack Compose UI, Accessibility detector service
├── iosApp/                       # SwiftUI application, DeviceActivity & widgets
└── .github/                      # CI/CD workflows, PR & issue templates
```

### Architectural Principles

1. **Pure Kotlin Domain**: Domain modules contain zero platform dependencies (no Android, Apple, or system frameworks).
2. **Ports & Adapters**: All platform interactions are abstracted behind testable ports (`ScrollEventSource`, `UsageTickSource`, `Enforcer`, `Clock`).
3. **Strict Privacy by Design**:
   - Zero on-screen text or user content is ever inspected, logged, or transmitted.
   - Only anonymous counters, timestamps, and target app identifiers are recorded.
   - Processing is 100% on-device.
4. **Data Accuracy Transparency**:
   - **Android**: Exact per-reel counting via accessibility node detection (`EXACT`).
   - **iOS**: Time-based estimates via DeviceActivity events (`ESTIMATED`).
   - Never mix exact and estimated data in the same trendline.

---

## 🧪 Test-Driven Development (TDD)

Unreel enforces strict TDD operating principles:

- **Red ➔ Green ➔ Refactor**: Write a failing test first, write minimum code to satisfy the test, then refactor.
- **Test Pyramid**: ~70% unit tests (`commonTest`), ~20% integration tests, ~10% UI/E2E tests.
- **Fakes Over Mocks**: Use verified in-memory fakes (`FakeClock`, `FakeEventSource`, etc.) for deterministic verification.
- **Coverage Gates**: Monitored via Kover to ensure high test coverage across core domain and data logic.

---

## 🚀 Building & Testing

### Prerequisites
- JDK 21 (Zulu recommended)
- Android Studio Ladybug or later / IntelliJ IDEA with Kotlin Multiplatform Mobile plugin
- Xcode 15+ (for iOS development on macOS)

### Running Tests

```bash
# Run all shared JVM unit tests
./gradlew :shared:core-model:jvmTest :shared:core-time:jvmTest :shared:domain:jvmTest :shared:testing:jvmTest

# Run shared Android host tests and app unit tests
./gradlew :shared:data:testAndroidHostTest :shared:presentation:testAndroidHostTest :androidApp:testDebugUnitTest

# Run all iOS simulator unit tests (macOS only)
./gradlew iosSimulatorArm64Test

# Run all checks across the project (lint + all tests)
./gradlew check
```

### Building Applications

```bash
# Build Android Debug APK
./gradlew :androidApp:assembleDebug

# Open iOS project in Xcode
open iosApp/iosApp.xcodeproj
```

---

## 🔒 Security & Privacy

Unreel is strictly privacy-preserving with local-only persistence and zero screen-text capture. Read our [SECURITY.md](./SECURITY.md) for full details.

---

## 🤝 Contributing & Repository Management

We follow strict TDD and [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` New user-facing feature
- `fix:` Bug fix
- `test:` Adding or refactoring tests
- `refactor:` Code change that neither fixes a bug nor adds a feature
- `docs:` Documentation changes
- `ci:` Changes to CI/CD workflows and repository automation
- `chore:` Changes to build process or auxiliary tools

Please refer to [CONTRIBUTING.md](./CONTRIBUTING.md) for pull request guidelines and [docs/GITHUB_SETUP.md](./docs/GITHUB_SETUP.md) for repository settings, branch protection rules, and CI automation.