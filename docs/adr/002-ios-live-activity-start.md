# ADR-002: iOS Live Activity Start Method

## Status
PROPOSED — awaiting spike execution on device

## Context
The spec calls for a Live Activity (Dynamic Island + Lock Screen) that shows live reel counts. On iOS, there are multiple ways to start a Live Activity, and not all work when the main app is in the background.

## Question
Which method of starting a Live Activity works reliably?
(a) App Intent run by a Shortcuts personal automation on 'App is opened'
(b) Push-to-start token via a backend
(c) Manually from the foreground app

## Method
**Spike S2** — one-day timebox:
1. Implement a minimal Live Activity with ActivityKit
2. Try starting via each method
3. For (a): create an App Intent, set up a Shortcuts personal automation, test triggering
4. For (b): request a push-to-start token and attempt to send via APNs
5. For (c): start from a button in the app
6. Test each with the app in background and foreground

## Measurements / Findings
_To be filled after spike execution_

## Result
_To be filled after spike execution_

## Decision
_To be filled after spike execution_

## Consequences
This determines how the iOS Live Activity is triggered and shapes the onboarding flow (if Shortcuts automation is needed, we must guide the user through setup).
