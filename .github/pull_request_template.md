## Description
<!-- Summary of changes made in this PR -->

## Motivation & Context
<!-- Why is this change required? What issue does it fix? -->

## Type of Change
- [ ] 🚀 New feature (non-breaking change which adds functionality)
- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] 🔨 Refactoring (no functional changes, code cleanup / structure)
- [ ] 🧪 Tests (adding missing tests or updating existing tests)
- [ ] 📝 Documentation (updates to README, ADR, specs, or comments)
- [ ] 🔧 Build / CI / Tooling (updates to Gradle, GitHub Actions, configs)

## TDD & Verification Plan
<!-- Every feature/bug fix must adhere to test-driven development -->
- [ ] **Red:** Failing test written first reproducing the issue or defining expected behavior
- [ ] **Green:** Minimal implementation written to pass the test
- [ ] **Refactor:** Code cleaned up while keeping tests green
- [ ] Unit tests pass locally (`./gradlew test` / `:shared:core-model:jvmTest`)
- [ ] Android build passes (`./gradlew :androidApp:assembleDebug`)
- [ ] iOS simulator tests pass (if applicable)

## Architectural Compliance (AGENTS.md)
- [ ] Pure Kotlin domain logic (no Android/Apple SDK leaks in domain modules)
- [ ] Dependencies injected via interfaces/ports
- [ ] Platform data sources strictly marked (`EXACT` vs `ESTIMATED`)
- [ ] PR is focused and adheres to small commit guidelines (< 400 lines changed)

## Screenshots / Evidence (if UI change)
<!-- Attach screenshots, videos, or test output -->
