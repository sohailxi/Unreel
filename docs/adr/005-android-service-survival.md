# ADR-005: Android Service Survival on OEM Skins

## Status
PROPOSED — awaiting spike execution on device

## Context
Many OEM Android skins (Xiaomi MIUI, Samsung One UI, Oppo ColorOS, Vivo FuntouchOS) aggressively kill background services. Our foreground service and accessibility service must survive for accurate counting.

## Question
Can our services survive 24 hours on at least three OEM skins? What kill events occur, and what mitigations are needed?

## Method
**Spike S5** — one-day timebox per device:
1. Deploy the foreground service and accessibility service to test devices
2. Run for 24 hours on at least three OEM skins:
   - Xiaomi (MIUI/HyperOS)
   - Samsung (One UI)
   - Oppo/Vivo (ColorOS/FuntouchOS)
3. Log all kill and restart events with timestamps
4. Note battery optimization settings and autostart behavior
5. Test with and without battery optimization exemption

## Measurements / Findings
_To be filled after spike execution_

### Expected data points per device:
- Kill count over 24h
- Time between kills (if any)
- Did the service auto-restart?
- Battery drain percentage
- Required user actions for autostart

## Result
_To be filled after spike execution_

## Decision
_To be filled after spike execution_

## Consequences
Determines:
- Whether we need OEM-specific autostart instructions in onboarding
- Whether a watchdog mechanism is needed
- Battery drain baseline for Section 6.9 non-functional requirements
