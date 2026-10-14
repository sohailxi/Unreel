package app.unreel.domain.engine

import app.unreel.model.Confidence
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.TrendDirection
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MetricsEngineTest {

    private val engine = MetricsEngine()

    @Test
    fun `trend with fewer than 5 days returns LOW confidence and STEADY`() {
        val dailyTotals = listOf(100L, 90L, 80L, 70L) // 4 days
        val result = engine.calculateTrend(dailyTotals)

        assertEquals(Confidence.LOW, result.confidence)
        assertEquals(TrendDirection.STEADY, result.direction)
        assertEquals(0.0, result.percentChange)
    }

    @Test
    fun `trend with 5 to 9 days returns MEDIUM confidence`() {
        val dailyTotals = listOf(100L, 90L, 80L, 70L, 60L) // 5 days, decreasing
        val result = engine.calculateTrend(dailyTotals)

        assertEquals(Confidence.MEDIUM, result.confidence)
        assertEquals(TrendDirection.IMPROVING, result.direction)
        assertTrue(result.percentChange < -5.0)
    }

    @Test
    fun `trend with 10 or more days returns HIGH confidence`() {
        // 14 days of steadily decreasing usage
        val dailyTotals = (14 downTo 1).map { (it * 10).toLong() }
        val result = engine.calculateTrend(dailyTotals)

        assertEquals(Confidence.HIGH, result.confidence)
        assertEquals(TrendDirection.IMPROVING, result.direction)
        assertTrue(result.percentChange < -5.0)
    }

    @Test
    fun `increasing usage trend returns SLIPPING direction`() {
        val dailyTotals = listOf(10L, 20L, 30L, 40L, 50L) // 5 days, increasing
        val result = engine.calculateTrend(dailyTotals)

        assertEquals(TrendDirection.SLIPPING, result.direction)
        assertTrue(result.percentChange > 5.0)
    }

    @Test
    fun `flat usage trend returns STEADY direction`() {
        val dailyTotals = listOf(50L, 51L, 49L, 50L, 50L)
        val result = engine.calculateTrend(dailyTotals)

        assertEquals(TrendDirection.STEADY, result.direction)
        assertTrue(result.percentChange in -5.0..5.0)
    }

    @Test
    fun `focus score computes 0 when usage is zero`() {
        val usage = DailyUsage(
            localDate = LocalDate(2025, 1, 15),
            appId = "instagram",
            reels = 0,
            durationMs = 0L,
            sessions = 0,
            opens = 0,
            source = DataSource.EXACT,
        )
        val score = engine.calculateFocusScore(
            dailyUsage = usage,
            goalReels = 50,
            goalMinutes = 30,
            lateNightDurationMs = 0L,
            longSessionDurationMs = 0L,
        )
        assertEquals(0, score.value)
        assertEquals(0.0, score.components.reelsVsGoal)
        assertEquals(0.0, score.components.timeVsGoal)
        assertEquals(0.0, score.components.lateNightShare)
        assertEquals(0.0, score.components.longSessionShare)
    }

    @Test
    fun `focus score correctly weights all four components`() {
        val usage = DailyUsage(
            localDate = LocalDate(2025, 1, 15),
            appId = "instagram",
            reels = 50, // at goal -> 1.0 (100% of goal)
            durationMs = 30 * 60_000L, // at goal -> 1.0 (100% of goal)
            sessions = 3,
            opens = 3,
            source = DataSource.EXACT,
        )
        // 50% of time was late night, 25% was in long sessions
        val lateNightMs = 15 * 60_000L
        val longSessionMs = (7.5 * 60_000L).toLong()

        val score = engine.calculateFocusScore(
            dailyUsage = usage,
            goalReels = 50,
            goalMinutes = 30,
            lateNightDurationMs = lateNightMs,
            longSessionDurationMs = longSessionMs,
        )

        // reelsVsGoal = 100, timeVsGoal = 100, lateNightShare = 50, longSessionShare = 25
        // Total = 0.40*100 + 0.30*100 + 0.20*50 + 0.10*25 = 82.5 -> 82 (banker's round half-to-even)
        assertEquals(82, score.value)
        assertEquals(100.0, score.components.reelsVsGoal)
        assertEquals(100.0, score.components.timeVsGoal)
        assertEquals(50.0, score.components.lateNightShare)
        assertEquals(25.0, score.components.longSessionShare)
    }
}
