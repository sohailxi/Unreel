# Unreel: AI Agent Standing Instructions

## 1. Name and positioning

**Primary name:** Unreel
**Tagline:** Know your scroll. Reclaim your time.

The name plays on 'reel' and 'unreel' (to unspool, to break free). It is short, easy to say, and works as an icon wordmark.

**Placeholder IDs:**
- iOS bundle ID: `app.unreel.ios`
- Android applicationId: `app.unreel.android`
- App Group: `group.app.unreel.shared`

## 2. Reality check: what each platform actually allows

### Product decisions

- **Android = exact counting.** Real per-reel counts, overlay popup, reels-only blocking.
- **iOS = estimated counting + system-level enforcement.** Time-based estimates, Live Activity, shields, widgets. Never present estimates as exact: store a `source` field (`EXACT` or `ESTIMATED`) on every record and never mix them in one trend line.
- **'All platforms'** means all reel apps (Instagram Reels, YouTube Shorts, TikTok, Facebook Reels, Snapchat Spotlight, and others added later through a remote rule pack) on both Android and iOS.

### Phase 0 spikes (go / no-go, one-day timeboxes)

1. **S1, iOS usage ticks:** DeviceActivityEvents at 1-minute increments
2. **S2, iOS Live Activity start:** App Intent + Shortcuts automation, push-to-start, foreground
3. **S3, iOS widget refresh:** Measure WidgetKit reload frequency
4. **S4, Android detector:** Record AccessibilityNodeInfo trees as JSON fixtures
5. **S5, Android survival:** 24-hour foreground + accessibility service on 3 OEM skins
6. **S6, entitlement:** Submit Family Controls (Distribution) request to Apple

## 3. Architecture

**Style:** Clean Architecture with unidirectional data flow (MVI). Business logic lives in shared Kotlin; UI is native on each platform.

```
unreel/
├─ build-logic/              convention plugins, version catalog
├─ shared/
│  ├─ core-model/            entities, value classes, error types
│  ├─ core-time/             Clock port, DayBoundary, timezone/DST logic
│  ├─ domain/                use cases, RuleEngine, MetricsEngine, repository interfaces
│  ├─ data/                  SQLDelight, repositories, settings, SnapshotPublisher
│  └─ presentation/          ViewModels exposing StateFlow (SKIE-friendly)
├─ androidApp/               Compose UI, AccessibilityService, overlay, Glance widgets,
│                            foreground service, enforcement
├─ iosApp/
│  ├─ Unreel/                SwiftUI app
│  ├─ UnreelWidgets/         WidgetKit widgets + Live Activity UI
│  ├─ UsageMonitor/          DeviceActivityMonitor extension
│  ├─ UsageReport/           DeviceActivityReport extension
│  ├─ ShieldConfig/          ShieldConfigurationDataSource extension
│  └─ ShieldAction/          ShieldActionDelegate extension
├─ docs/adr/                 architecture decision records
└─ .github/workflows/        CI
```

### Hard rules

- The domain layer is **pure Kotlin**: no Android imports, no Apple imports, no `Clock.System`, no hard-coded dispatchers.
- Platform behaviour sits behind **ports** (interfaces) implemented natively: `ScrollEventSource`, `ForegroundAppSource`, `UsageTickSource`, `Enforcer`, `LiveSurfacePublisher`, `Clock`, `NotificationPort`.
- **Event sourcing** for usage: store immutable raw events (`ReelEvent`, `UsageTick`) and derive aggregates.
- **Snapshot pattern** for iOS extensions: widgets only decode a small versioned JSON Snapshot from the App Group; they do not link the full KMP framework.
- On iOS the SQLite file lives in the App Group container (WAL mode).
- **One data flow:** platform event → use case → repository → SnapshotPublisher → surfaces.

### Core domain model (names are binding)

- `TrackedApp(id, platform, displayName, packageOrTokenRef, category)`
- `ReelEvent(id, appId, tsUtc, dwellMs, source)`
- `UsageTick(id, appId, tsUtc, activeSeconds, source)`
- `Session(id, appId, startUtc, endUtc, reelCount, durationMs, source)`
- `DailyUsage(localDate, appId, reels, durationMs, sessions, opens, source)`
- `LimitRule(id, appIds, kind, params, schedule, strictness, enabled)`
- `Decision` — sealed type: `Allow`, `Nudge(reason)`, `Friction(kind)`, `Block(until, reason)`
- `TrendResult(direction, percentChange, confidence)`
- `FocusScore(value, components)`

## 4. Tech stack

| Concern | Choice |
|---|---|
| Language and build | Kotlin 2.x, Gradle version catalog, convention plugins |
| Async | kotlinx-coroutines + Flow |
| Time | kotlinx-datetime |
| Serialization | kotlinx-serialization |
| Database | SQLDelight |
| Preferences | multiplatform-settings |
| DI | Koin (KMP) |
| Swift interop | SKIE (Touchlab) |
| ViewModels | androidx.lifecycle ViewModel (KMP) |
| Logging | Kermit |
| Android UI | Compose + Material 3, Vico, Glance, WorkManager |
| iOS UI | SwiftUI, Swift Charts, WidgetKit, ActivityKit, FamilyControls |
| Shared tests | kotlin.test, Turbine, coroutines-test, Kotest property testing |
| Android tests | Robolectric, Compose UI tests, Roborazzi/Paparazzi |
| iOS tests | XCTest / Swift Testing, swift-snapshot-testing |
| Static analysis | detekt, ktlint (Spotless), SwiftLint, SwiftFormat, Kover |
| CI/CD | GitHub Actions (macOS runners for iOS), Fastlane |

## 5. TDD operating rules

1. **Red, Green, Refactor.** Write one failing test → show it failing → minimum code to pass → refactor → commit.
2. **Test pyramid:** ~70% unit (commonTest), ~20% integration, ~10% UI/screenshot/E2E.
3. **Inject everything non-deterministic:** Clock, TimeZone, dispatchers, randomness. Use `FakeClock`.
4. **Fakes over mocks.** Maintain `FakeScrollEventSource`, `FakeForegroundAppSource`, `FakeEnforcer`, `InMemoryRepositories` in a shared testing module.
5. **Golden fixtures:** Detector tests replay recorded accessibility trees (JSON) and assert exact reel counts.
6. **Property-based tests** for: rule engine, aggregation, day boundaries, trend metrics.
7. **Every bug starts as a failing regression test.**
8. **Coverage gates (Kover):** domain 90%+, data 80%+, presentation 80%+. CI fails below the gate.
9. **Definition of Done:** tests green on JVM and iOS simulator; lint clean; no new warnings; public API documented; ADR if decision made; accessibility labels on UI; Conventional Commits.
10. **Small commits and PRs** (under ~400 changed lines).

## Master prompt (for phase work)

> You are a senior Kotlin Multiplatform, SwiftUI and Android engineer building 'Unreel', an app that counts and limits short-video (reels) scrolling. Follow the project spec exactly.
>
> Rules:
> 1. Strict TDD: write a failing test first, show it failing, write the minimum code to pass, refactor, commit.
> 2. Domain code is pure Kotlin. Inject Clock, TimeZone and dispatchers. Use ports for all platform behaviour.
> 3. iOS cannot count reels exactly: mark iOS counts ESTIMATED and never mix them with EXACT data.
> 4. Never read or store on-screen text or user content. Store counters, timestamps and package names only.
> 5. Do not invent view IDs, API behaviour or entitlements. If unsure, write a spike or an ADR and ask me.
> 6. Work only on the phase I give you. Keep PRs under ~400 changed lines.
> 7. End every task with: tests run and results, files changed, open questions.
> 8. Keep detekt, ktlint and SwiftLint clean and respect the coverage gates.
