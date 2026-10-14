## Summary & Motivation
<!-- Concise explanation of the problem, motivation, or user story addressed by this PR. Include reference to issue (e.g. Closes #12). -->

## Type of Change
- [ ] 🚀 New feature (non-breaking addition to functionality)
- [ ] 🐛 Bug fix (regression fix following TDD)
- [ ] 🔨 Refactoring (restructuring with no functional behavior changes)
- [ ] 🧪 Tests (adding missing unit, integration, or property tests)
- [ ] 📝 Documentation (ADRs, markdown, or architectural docs)
- [ ] 🔧 Build / CI / Tooling (Gradle, GitHub Actions, workflows, or scripts)

## TDD Verification & Test Results
<!-- Unreel enforces strict Test-Driven Development (Red -> Green -> Refactor) -->
- [ ] **Red:** Failing test written first reproducing the issue or defining new behavior
- [ ] **Green:** Minimum code written to pass the test
- [ ] **Refactor:** Code cleaned up and decoupled while keeping all tests green
- [ ] **Local Verification Passed:**
  - [ ] Shared JVM tests: `./gradlew :shared:core-model:jvmTest :shared:core-time:jvmTest :shared:domain:jvmTest :shared:testing:jvmTest`
  - [ ] Android Host tests: `./gradlew :shared:data:testAndroidHostTest :shared:presentation:testAndroidHostTest`
  - [ ] Android App tests & Lint: `./gradlew :androidApp:testDebugUnitTest :androidApp:lintDebug :androidApp:assembleDebug`
  - [ ] iOS Simulator tests: `./gradlew iosSimulatorArm64Test`
  - [ ] iOS Native App Build: `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -destination 'generic/platform=iOS Simulator' build CODE_SIGNING_ALLOWED=NO`

## Architectural & Privacy Compliance (AGENTS.md)
- [ ] **Pure Kotlin Domain:** No Android, Apple, or platform imports inside `shared:core-model` or `shared:domain`
- [ ] **Ports & Adapters:** All platform integrations are behind interfaces in `domain/port`
- [ ] **Source Tagging:** Data sources strictly tagged as `EXACT` (Android) or `ESTIMATED` (iOS), never blended into a single raw stream
- [ ] **Strict Privacy:** Zero inspection, storage, or transmission of on-screen text, user video titles, or personal content
- [ ] **PR Size Budget:** PR is focused and stays within small change budget (< ~400 lines changed)

## UI / Artifact Evidence (if applicable)
<!-- Attach screenshots, videos, or test execution output -->
