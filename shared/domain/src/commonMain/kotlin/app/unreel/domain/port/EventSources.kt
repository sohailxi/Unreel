package app.unreel.domain.port

import app.unreel.model.ReelEvent
import kotlinx.coroutines.flow.Flow

/**
 * Port for receiving scroll/reel detection events from the platform.
 * Android: AccessibilityService-based detector.
 * iOS: not applicable (iOS uses UsageTickSource instead).
 */
interface ScrollEventSource {
    val events: Flow<ReelEvent>
}

/**
 * Port for knowing which app is in the foreground.
 * Android: UsageStatsManager + accessibility window events.
 * iOS: opaque FamilyControls tokens.
 */
interface ForegroundAppSource {
    data class ForegroundApp(val packageOrToken: String, val since: kotlinx.datetime.Instant)

    val foregroundApp: Flow<ForegroundApp?>
}
