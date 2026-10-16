package app.unreel.android.ui

import app.unreel.model.Confidence
import app.unreel.model.TrendDirection
import app.unreel.model.TrendResult
import kotlin.test.Test
import kotlin.test.assertEquals

class StatsScreenTest {

    @Test
    fun `formatTrendPercent formats positive and negative percentages`() {
        assertEquals("+15.0%", formatTrendPercent(15.0))
        assertEquals("-10.5%", formatTrendPercent(-10.5))
        assertEquals("0.0%", formatTrendPercent(0.0))
    }

    @Test
    fun `formatTrendDescription provides readable trend summary`() {
        val improving = TrendResult(
            direction = TrendDirection.IMPROVING,
            percentChange = -15.0,
            confidence = Confidence.HIGH,
        )
        assertEquals("Improving (-15.0%)", formatTrendDescription(improving))

        val slipping = TrendResult(
            direction = TrendDirection.SLIPPING,
            percentChange = 20.0,
            confidence = Confidence.MEDIUM,
        )
        assertEquals("Slipping (+20.0%)", formatTrendDescription(slipping))

        val lowConfidence = TrendResult(
            direction = TrendDirection.STEADY,
            percentChange = 0.0,
            confidence = Confidence.LOW,
        )
        assertEquals("Not enough data", formatTrendDescription(lowConfidence))
    }

    @Test
    fun `formatSessionsAndOpens formats compact summary for narrower screens`() {
        assertEquals("3 ses • 12 opens", formatSessionsAndOpens(sessions = 3, opens = 12))
        assertEquals("1 ses • 1 open", formatSessionsAndOpens(sessions = 1, opens = 1))
    }

    @Test
    fun `getStatsAppLabel returns friendly label for specific apps and default for all`() {
        assertEquals("All Feeds & Reels", getStatsAppLabel(null))
        assertEquals("Instagram Reels", getStatsAppLabel("com.instagram.android"))
        assertEquals("Reddit", getStatsAppLabel("com.reddit.frontpage"))
    }
}

