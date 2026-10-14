package app.unreel.android.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Stub AccessibilityService for reel detection.
 * Implementation will be added in Phase 4.
 *
 * Privacy constraints (non-negotiable):
 * - Restricted to tracked app allowlist via android:packageNames
 * - Never reads, logs or stores node text, messages or user content
 * - Stores only counters, timestamps and package names
 * - No raw accessibility data leaves the device
 */
class ReelDetectorService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Phase 4: detector engine will process events here
    }

    override fun onInterrupt() {
        // Phase 4: handle service interruption
    }
}
