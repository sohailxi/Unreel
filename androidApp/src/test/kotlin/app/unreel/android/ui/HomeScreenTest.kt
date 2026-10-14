package app.unreel.android.ui

import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.model.FocusScore
import app.unreel.model.Session
import app.unreel.presentation.home.HomeState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeScreenTest {

    @Test
    fun `formatDwellTime formats milliseconds into readable minutes and seconds`() {
        assertEquals("0m", formatDwellTime(0L))
        assertEquals("1m 30s", formatDwellTime(90_000L))
        assertEquals("15m", formatDwellTime(900_000L))
        assertEquals("1h 15m", formatDwellTime(4_500_000L))
    }

    @Test
    fun `calculateGoalProgress calculates clamped progress fraction`() {
        assertEquals(0f, calculateGoalProgress(currentReels = 0, targetReels = 50))
        assertEquals(0.5f, calculateGoalProgress(currentReels = 25, targetReels = 50))
        assertEquals(1.0f, calculateGoalProgress(currentReels = 50, targetReels = 50))
        assertEquals(1.0f, calculateGoalProgress(currentReels = 75, targetReels = 50))
        assertEquals(0f, calculateGoalProgress(currentReels = 10, targetReels = 0))
    }

    @Test
    fun `homeState decision detection`() {
        val allowState = HomeState(currentDecision = Decision.Allow)
        assertFalse(allowState.isRestricted)

        val blockState = HomeState(
            currentDecision = Decision.Block(
                until = null,
                reason = "Daily limit",
                ruleId = "r1",
                canBypass = true,
                passesRemaining = 1,
            )
        )
        assertTrue(blockState.isRestricted)
    }
}
