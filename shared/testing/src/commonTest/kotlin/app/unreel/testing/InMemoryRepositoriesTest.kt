package app.unreel.testing

import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.LimitRule
import app.unreel.model.ReelEvent
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Session
import app.unreel.model.Strictness
import app.unreel.model.UsageTick
import app.unreel.time.AppClock
import app.unreel.time.DayBoundary
import app.unreel.domain.repository.UsageRepository
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InMemoryRepositoriesTest {

    @Test
    fun testInMemoryUsageRepositoryStoresAndQueriesUsage() = runTest {
        val repo = InMemoryUsageRepository()

        val event = ReelEvent(
            id = "event-1",
            appId = "instagram",
            tsUtc = Instant.parse("2025-01-15T10:00:00Z"),
            dwellMs = 4500L,
            source = DataSource.EXACT,
        )
        repo.recordReelEvent(event)
        assertEquals(listOf(event), repo.recordedEvents)

        val daily = DailyUsage(
            localDate = LocalDate(2025, 1, 15),
            appId = "instagram",
            reels = 10,
            durationMs = 45_000L,
            sessions = 1,
            opens = 1,
            source = DataSource.EXACT,
        )
        repo.saveDailyUsage(daily)
        assertEquals(daily, repo.getDailyUsage(LocalDate(2025, 1, 15), "instagram"))
        assertEquals(daily, repo.observeDailyUsage(LocalDate(2025, 1, 15), "instagram").first())

        val session = Session(
            id = "session-1",
            appId = "instagram",
            startUtc = Instant.parse("2025-01-15T10:00:00Z"),
            endUtc = Instant.parse("2025-01-15T10:10:00Z"),
            reelCount = 10,
            durationMs = 600_000L,
            source = DataSource.EXACT,
        )
        repo.saveSession(session)
        assertEquals(session, repo.getActiveSession("instagram"))

        repo.endSession("session-1")
        assertNull(repo.getActiveSession("instagram"))
        assertEquals(1, repo.getRecentSessions().size)
    }

    @Test
    fun testInMemoryRuleRepositoryManagesRules() = runTest {
        val repo = InMemoryRuleRepository()

        val rule = LimitRule(
            id = "rule-1",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 100),
            schedule = null,
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )
        repo.saveRule(rule)

        assertEquals(1, repo.getRules().size)
        assertEquals(listOf(rule), repo.getRulesForApp("instagram"))
        assertTrue(repo.getRulesForApp("tiktok").isEmpty())

        repo.deleteRule("rule-1")
        assertTrue(repo.getRules().isEmpty())
    }

    @Test
    fun testInMemorySettingsRepositoryTracksPreferences() = runTest {
        val repo = InMemorySettingsRepository()

        assertEquals(4, repo.getDayStartHour())
        repo.setDayStartHour(5)
        assertEquals(5, repo.getDayStartHour())
        assertEquals(5, repo.observeDayStartHour().first())

        repo.setFocusGoalReels(60)
        assertEquals(60, repo.getFocusGoalReels())

        repo.setFocusGoalMinutes(45)
        assertEquals(45, repo.getFocusGoalMinutes())

        assertTrue(repo.isAppTracked("instagram"))
        repo.setAppTracked("instagram", false)
        assertFalse(repo.isAppTracked("instagram"))
    }
}
