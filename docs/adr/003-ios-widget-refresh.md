# ADR-003: iOS WidgetKit Refresh Frequency

## Status
PROPOSED — awaiting spike execution on device

## Context
WidgetKit has a reload budget that limits how often widgets can refresh. The spec wants widgets that stay current with usage data, but we need to know the actual limits.

## Question
How often does WidgetKit actually honour reloads triggered from the app and from the DeviceActivityMonitor extension? Can push-driven reloads supplement the budget?

## Method
**Spike S3** — one-day timebox:
1. Create a minimal WidgetKit widget
2. Trigger reloads from the main app at varying intervals
3. Trigger reloads from the monitor extension
4. Log actual reload times to the App Group
5. Compare requested vs actual refreshes over 24 hours
6. Test Text(timerInterval:) for live elapsed time without reloads

## Measurements / Findings
_To be filled after spike execution_

## Result
_To be filled after spike execution_

## Decision
_To be filled after spike execution_

## Consequences
Determines our widget refresh strategy: aggressive reloads, timer-style text, push-driven reloads, or a combination.
