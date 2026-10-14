package app.unreel.model

import kotlinx.serialization.Serializable

/**
 * The source of a usage data point.
 * Android provides exact reel counts; iOS provides time-based estimates.
 * EXACT and ESTIMATED data must never be mixed in the same trend line.
 */
@Serializable
enum class DataSource {
    /** Android: real per-reel counts from AccessibilityService */
    EXACT,
    /** iOS: time-based estimates derived from DeviceActivity ticks */
    ESTIMATED,
}
