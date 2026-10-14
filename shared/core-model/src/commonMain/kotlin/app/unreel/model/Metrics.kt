package app.unreel.model

import kotlinx.serialization.Serializable

/**
 * Trend direction for usage comparison, per Section 6.5.
 * Based on least-squares slope of daily totals over last 14 days.
 */
@Serializable
enum class TrendDirection {
    IMPROVING,
    STEADY,
    SLIPPING,
}

@Serializable
enum class Confidence {
    LOW,
    MEDIUM,
    HIGH,
}

/**
 * Result of trend analysis.
 * Requires ≥5 days of data; otherwise confidence is LOW and the badge reads 'Not enough data'.
 */
@Serializable
data class TrendResult(
    val direction: TrendDirection,
    val percentChange: Double,
    val confidence: Confidence,
)

/**
 * Doomscroll Score (0–100, lower is better).
 * Components: 40% reels vs goal, 30% time vs goal, 20% late-night share, 10% long sessions.
 */
@Serializable
data class FocusScore(
    val value: Int,
    val components: FocusScoreComponents,
)

@Serializable
data class FocusScoreComponents(
    val reelsVsGoal: Double,
    val timeVsGoal: Double,
    val lateNightShare: Double,
    val longSessionShare: Double,
)
