package app.unreel.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * An app that Unreel tracks for reel/short-video usage.
 */
@Serializable
data class TrackedApp(
    val id: String,
    val platform: Platform,
    val displayName: String,
    val packageOrTokenRef: String,
    val category: String,
    val enabled: Boolean = true,
)

/**
 * An immutable event recording a single reel scroll on Android (exact),
 * or an inferred reel count from a usage tick on iOS (estimated).
 */
@Serializable
data class ReelEvent(
    val id: String,
    val appId: String,
    val tsUtc: Instant,
    val dwellMs: Long,
    val source: DataSource,
)

/**
 * An immutable 1-minute usage tick from iOS DeviceActivity monitoring.
 */
@Serializable
data class UsageTick(
    val id: String,
    val appId: String,
    val tsUtc: Instant,
    val activeSeconds: Int,
    val source: DataSource,
)

/**
 * A contiguous session of reel scrolling.
 * Starts when the reel surface becomes visible, ends after [sessionGapMs]
 * of inactivity or when the user leaves the app.
 */
@Serializable
data class Session(
    val id: String,
    val appId: String,
    val startUtc: Instant,
    val endUtc: Instant,
    val reelCount: Int,
    val durationMs: Long,
    val source: DataSource,
)

/**
 * Materialized daily aggregate, derived from raw events.
 * Rebuilt when day start, calibration, or time zone changes.
 */
@Serializable
data class DailyUsage(
    val localDate: LocalDate,
    val appId: String,
    val reels: Int,
    val durationMs: Long,
    val sessions: Int,
    val opens: Int,
    val source: DataSource,
)
