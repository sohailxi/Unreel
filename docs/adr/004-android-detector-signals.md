# ADR-004: Android Accessibility Detector Signals

## Status
PROPOSED — awaiting spike execution on device

## Context
Android reel detection works by observing AccessibilityNodeInfo trees while the user scrolls reels. We need to identify stable signals (view IDs, class names, scroll event indices) for each app.

## Question
Can we reliably detect a page change (new reel) via AccessibilityService events for Instagram Reels, YouTube Shorts, and TikTok? What are the stable signals?

## Method
**Spike S4** — one-day timebox:
1. Create a debug AccessibilityService that dumps AccessibilityNodeInfo trees as JSON
2. Scroll through reels on current versions of Instagram, YouTube, and TikTok
3. Record the JSON fixtures for each app, each scroll transition
4. Analyze: find view IDs, class names, content descriptions, and event types that signal a page change
5. Test: do these signals work for both forward and backward scrolling?
6. Store fixtures in `androidApp/src/test/resources/fixtures/`

## Measurements / Findings
_To be filled after spike execution_

### Expected fixture structure:
```
fixtures/
├─ instagram/
│  ├─ v350/
│  │  ├─ swipe-5-reels.json
│  │  └─ scroll-back.json
│  └─ metadata.json
├─ youtube/
│  └─ ...
└─ tiktok/
   └─ ...
```

## Result
_To be filled after spike execution_

## Decision
_To be filled after spike execution_

## Consequences
The recorded fixtures become golden test data for Phase 4's DetectorEngine. Rule packs will be derived from these signals, not invented.
