package app.unreel.domain.engine

import app.unreel.model.Confidence
import app.unreel.model.DailyUsage
import app.unreel.model.FocusScore
import app.unreel.model.FocusScoreComponents
import app.unreel.model.TrendDirection
import app.unreel.model.TrendResult
import kotlin.math.round

/**
 * Calculates usage trends via least-squares linear regression and computes
 * the weighted Doomscroll/Focus Score (0–100, lower is better).
 */
class MetricsEngine {

    /**
     * Calculates 14-day (or shorter) usage trend direction, percentage change, and confidence.
     * Requires at least 5 days of data; otherwise confidence is LOW and direction is STEADY.
     */
    fun calculateTrend(dailyTotals: List<Long>): TrendResult {
        val n = dailyTotals.size
        if (n < 5) {
            return TrendResult(
                direction = TrendDirection.STEADY,
                percentChange = 0.0,
                confidence = Confidence.LOW,
            )
        }

        val xMean = (n - 1) / 2.0
        val yMean = dailyTotals.map { it.toDouble() }.average()

        if (yMean == 0.0) {
            return TrendResult(
                direction = TrendDirection.STEADY,
                percentChange = 0.0,
                confidence = if (n >= 10) Confidence.HIGH else Confidence.MEDIUM,
            )
        }

        var numerator = 0.0
        var denominator = 0.0

        for (i in 0 until n) {
            val xDiff = i - xMean
            val yDiff = dailyTotals[i] - yMean
            numerator += xDiff * yDiff
            denominator += xDiff * xDiff
        }

        val slope = if (denominator != 0.0) numerator / denominator else 0.0
        val totalChange = slope * (n - 1)
        val percentChange = (totalChange / yMean) * 100.0

        val direction = when {
            percentChange <= -5.0 -> TrendDirection.IMPROVING
            percentChange >= 5.0 -> TrendDirection.SLIPPING
            else -> TrendDirection.STEADY
        }

        val confidence = if (n >= 10) Confidence.HIGH else Confidence.MEDIUM

        return TrendResult(
            direction = direction,
            percentChange = round(percentChange * 10.0) / 10.0,
            confidence = confidence,
        )
    }

    /**
     * Calculates the Focus/Doomscroll score (0–100, lower is better).
     * Components:
     * - 40% reels vs goal
     * - 30% time vs goal
     * - 20% late-night share
     * - 10% long sessions share
     */
    fun calculateFocusScore(
        dailyUsage: DailyUsage,
        goalReels: Int,
        goalMinutes: Int,
        lateNightDurationMs: Long,
        longSessionDurationMs: Long,
    ): FocusScore {
        val reelsVsGoal = if (goalReels > 0) {
            ((dailyUsage.reels.toDouble() / goalReels) * 100.0).coerceAtMost(200.0)
        } else {
            0.0
        }

        val durationMinutes = dailyUsage.durationMs.toDouble() / 60_000.0
        val timeVsGoal = if (goalMinutes > 0) {
            ((durationMinutes / goalMinutes) * 100.0).coerceAtMost(200.0)
        } else {
            0.0
        }

        val totalDurationMs = dailyUsage.durationMs.toDouble()
        val lateNightShare = if (totalDurationMs > 0) {
            ((lateNightDurationMs.toDouble() / totalDurationMs) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        val longSessionShare = if (totalDurationMs > 0) {
            ((longSessionDurationMs.toDouble() / totalDurationMs) * 100.0).coerceIn(0.0, 100.0)
        } else {
            0.0
        }

        val weightedSum = (0.40 * reelsVsGoal) +
            (0.30 * timeVsGoal) +
            (0.20 * lateNightShare) +
            (0.10 * longSessionShare)

        val scoreValue = round(weightedSum).toInt().coerceIn(0, 100)

        return FocusScore(
            value = scoreValue,
            components = FocusScoreComponents(
                reelsVsGoal = round(reelsVsGoal * 10.0) / 10.0,
                timeVsGoal = round(timeVsGoal * 10.0) / 10.0,
                lateNightShare = round(lateNightShare * 10.0) / 10.0,
                longSessionShare = round(longSessionShare * 10.0) / 10.0,
            ),
        )
    }
}
