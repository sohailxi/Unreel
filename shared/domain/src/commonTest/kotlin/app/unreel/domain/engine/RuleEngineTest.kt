package app.unreel.domain.engine

import app.unreel.model.DataSource
import app.unreel.model.DailyUsage
import app.unreel.model.Decision
import app.unreel.model.FrictionKind
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Schedule
import app.unreel.model.Session
import app.unreel.model.Strictness
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuleEngineTest {

    private val engine = RuleEngine()
    private val timeZone = TimeZone.UTC
    private val currentTime = Instant.parse("2025-01-15T12:00:00Z") // Wednesday (Day 3)
    private val today = LocalDate(2025, 1, 15)

    @Test
    fun `no rules returns Allow`() {
        val decision = engine.evaluate(
            appId = "instagram",
            currentTime = currentTime,
            timeZone = timeZone,
            dailyUsage = null,
            currentSession = null,
            rules = emptyList<LimitRule>(),
        )
        assertEquals(Decision.Allow, decision)
    }

    @Test
    fun `disabled rule returns Allow`() {
        val rule = LimitRule(
            id = "rule-1",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 50),
            schedule = null,
            strictness = Strictness.HARD_BLOCK,
            enabled = false,
        )
        val usage = DailyUsage(today, "instagram", reels = 60, durationMs = 120_000, sessions = 3, opens = 3, source = DataSource.EXACT)
        val decision = engine.evaluate("instagram", currentTime, timeZone, usage, null, listOf(rule))
        assertEquals(Decision.Allow, decision)
    }

    @Test
    fun `daily reel limit under limit returns Allow`() {
        val rule = LimitRule(
            id = "rule-1",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 50),
            schedule = null,
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )
        val usage = DailyUsage(today, "instagram", reels = 49, durationMs = 100_000, sessions = 2, opens = 2, source = DataSource.EXACT)
        val decision = engine.evaluate("instagram", currentTime, timeZone, usage, null, listOf(rule))
        assertEquals(Decision.Allow, decision)
    }

    @Test
    fun `daily reel limit reached returns Block with rule details`() {
        val rule = LimitRule(
            id = "rule-1",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 50),
            schedule = null,
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )
        val usage = DailyUsage(today, "instagram", reels = 50, durationMs = 100_000, sessions = 2, opens = 2, source = DataSource.EXACT)
        val decision = engine.evaluate("instagram", currentTime, timeZone, usage, null, listOf(rule))

        assertTrue(decision is Decision.Block)
        assertEquals("rule-1", decision.ruleId)
        assertEquals(false, decision.canBypass)
    }

    @Test
    fun `daily time limit reached with Nudge strictness returns Nudge`() {
        val rule = LimitRule(
            id = "rule-time",
            appIds = listOf("youtube"),
            kind = RuleKind.DAILY_TIME_LIMIT,
            params = RuleParams(limitMinutes = 30),
            schedule = null,
            strictness = Strictness.NUDGE,
            enabled = true,
        )
        val usage = DailyUsage(today, "youtube", reels = 20, durationMs = 30 * 60_000L, sessions = 1, opens = 1, source = DataSource.EXACT)
        val decision = engine.evaluate("youtube", currentTime, timeZone, usage, null, listOf(rule))

        assertTrue(decision is Decision.Nudge)
        assertEquals("rule-time", decision.ruleId)
    }

    @Test
    fun `session cap reached with Friction strictness returns Friction`() {
        val rule = LimitRule(
            id = "rule-session",
            appIds = listOf("tiktok"),
            kind = RuleKind.SESSION_CAP,
            params = RuleParams(limitReels = 25, limitMinutes = 15),
            schedule = null,
            strictness = Strictness.FRICTION,
            enabled = true,
        )
        val session = Session(
            id = "sess-1",
            appId = "tiktok",
            startUtc = Instant.parse("2025-01-15T11:40:00Z"),
            endUtc = currentTime,
            reelCount = 25,
            durationMs = 20 * 60_000L,
            source = DataSource.EXACT,
        )
        val decision = engine.evaluate("tiktok", currentTime, timeZone, null, session, listOf(rule))

        assertTrue(decision is Decision.Friction)
        assertEquals(FrictionKind.BREATHING_PAUSE, decision.kind)
        assertEquals("rule-session", decision.ruleId)
    }

    @Test
    fun `schedule window blocks usage during scheduled hours`() {
        val rule = LimitRule(
            id = "rule-night",
            appIds = listOf("instagram"),
            kind = RuleKind.SCHEDULE_WINDOW,
            params = RuleParams(),
            schedule = Schedule(startTime = "22:00", endTime = "07:00"),
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )
        // 23:30 is within 22:00 - 07:00 window
        val nightTime = Instant.parse("2025-01-15T23:30:00Z")
        val decisionDuring = engine.evaluate("instagram", nightTime, timeZone, null, null, listOf(rule))
        assertTrue(decisionDuring is Decision.Block)

        // 12:00 is outside 22:00 - 07:00 window
        val dayTime = Instant.parse("2025-01-15T12:00:00Z")
        val decisionOutside = engine.evaluate("instagram", dayTime, timeZone, null, null, listOf(rule))
        assertEquals(Decision.Allow, decisionOutside)
    }

    @Test
    fun `schedule window respects days of week`() {
        val rule = LimitRule(
            id = "rule-weekday",
            appIds = listOf("instagram"),
            kind = RuleKind.SCHEDULE_WINDOW,
            params = RuleParams(),
            schedule = Schedule(
                startTime = "09:00",
                endTime = "17:00",
                daysOfWeek = listOf(1, 2, 3, 4, 5), // Monday to Friday
            ),
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )
        // Wednesday (Day 3) 10:00 -> blocked
        val wednesdayTime = Instant.parse("2025-01-15T10:00:00Z")
        val wednesdayDecision = engine.evaluate("instagram", wednesdayTime, timeZone, null, null, listOf(rule))
        assertTrue(wednesdayDecision is Decision.Block)

        // Saturday (Day 6, 2025-01-18) 10:00 -> allowed
        val saturdayTime = Instant.parse("2025-01-18T10:00:00Z")
        val saturdayDecision = engine.evaluate("instagram", saturdayTime, timeZone, null, null, listOf(rule))
        assertEquals(Decision.Allow, saturdayDecision)
    }

    @Test
    fun `strictest rule wins when multiple rules trigger`() {
        val nudgeRule = LimitRule(
            id = "rule-nudge",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 10),
            schedule = null,
            strictness = Strictness.NUDGE,
            enabled = true,
        )
        val blockRule = LimitRule(
            id = "rule-block",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_TIME_LIMIT,
            params = RuleParams(limitMinutes = 15),
            schedule = null,
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )
        // Usage exceeds both limits
        val usage = DailyUsage(today, "instagram", reels = 20, durationMs = 20 * 60_000L, sessions = 1, opens = 1, source = DataSource.EXACT)
        val decision = engine.evaluate("instagram", currentTime, timeZone, usage, null, listOf(nudgeRule, blockRule))

        assertTrue(decision is Decision.Block)
        assertEquals("rule-block", decision.ruleId)
    }

    @Test
    fun `soft block indicates bypass possibility and passes remaining`() {
        val rule = LimitRule(
            id = "rule-soft",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 30, maxPasses = 2),
            schedule = null,
            strictness = Strictness.SOFT_BLOCK,
            enabled = true,
        )
        val usage = DailyUsage(today, "instagram", reels = 35, durationMs = 60_000L, sessions = 1, opens = 1, source = DataSource.EXACT)
        val decision = engine.evaluate("instagram", currentTime, timeZone, usage, null, listOf(rule), bypassPassesUsed = 1)

        assertTrue(decision is Decision.Block)
        assertTrue(decision.canBypass)
        assertEquals(1, decision.passesRemaining)
    }
}
