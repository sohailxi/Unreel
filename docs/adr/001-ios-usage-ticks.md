# ADR-001: iOS Usage Ticks via DeviceActivityEvents

## Status
PROPOSED — awaiting spike execution on device

## Context
iOS cannot observe another app's UI. The only way to track usage is through the Screen Time APIs (FamilyControls + DeviceActivity). We need to determine if scheduling DeviceActivityEvents at 1-minute increments is reliable enough for estimated reel counting.

## Question
Can we schedule DeviceActivityEvents at 1-minute increments for selected apps, and does `eventDidReachThreshold` fire reliably in the DeviceActivityMonitor extension? How many events does the system accept? What is the battery impact?

## Method
**Spike S1** — one-day timebox:
1. Create a minimal DeviceActivityMonitor extension
2. Request FamilyControls individual authorization
3. Schedule events at 1-minute thresholds for 2-3 test apps
4. Run for several hours, logging every callback to the App Group
5. Compare expected vs actual callback count
6. Monitor battery usage in Settings

## Measurements / Findings
_To be filled after spike execution_

## Result
_To be filled after spike execution_

## Decision
_To be filled after spike execution_

## Consequences
If reliable: this becomes our iOS usage tracking pipeline.
If unreliable: we need to explore alternatives (background app refresh polling, or accepting coarser granularity).
