package app.unreel.domain.usecase

import app.unreel.domain.engine.RuleEngine
import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.model.LimitRule
import app.unreel.model.ReelEvent
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Session
import app.unreel.model.Strictness
import app.unreel.model.UsageTick
import app.unreel.time.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class IngestUsageTickUseCaseTest {

    private class TestClock(var currentInstant: Instant) : AppClock {
        override fun now(): Instant = currentInstant
        override fun timeZone(): TimeZone = TimeZone.UTC
    }

    private class TestUsageRepository : UsageRepository {
        val ticks = mutableListOf<UsageTick>()
        var activeSession: Session? = null
        var dailyUsage: DailyUsage? = null

        override suspend fun recordReelEvent(event: ReelEvent) {}

        override suspend fun recordUsageTick(tick: UsageTick) {
            ticks.add(tick)
        }

        override suspend fun getDailyUsage(date: LocalDate, appId: String?): DailyUsage? = dailyUsage

        override fun observeDailyUsage(date: LocalDate, appId: String?): Flow<DailyUsage?> = flowOf(dailyUsage)

        override suspend fun saveDailyUsage(dailyUsage: DailyUsage) {
            this.dailyUsage = dailyUsage
        }

        override suspend fun getDailyUsageHistory(from: LocalDate, to: LocalDate, appId: String?): List<DailyUsage> = emptyList()

        override suspend fun getActiveSession(appId: String): Session? = activeSession

        override suspend fun saveSession(session: Session) {
            activeSession = session
        }

        override suspend fun endSession(sessionId: String) {
            activeSession = null
        }

        override suspend fun getRecentSessions(limit: Int): List<Session> = emptyList()
    }

    private class TestSettingsRepository : SettingsRepository {
        var dayStartHour: Int = 4
        var tracked: Boolean = true

        override suspend fun getDayStartHour(): Int = dayStartHour
        override suspend fun setDayStartHour(hour: Int) { dayStartHour = hour }
        override fun observeDayStartHour(): Flow<Int> = flowOf(dayStartHour)
        override suspend fun getFocusGoalReels(): Int = 50
        override suspend fun setFocusGoalReels(reels: Int) {}
        override fun observeFocusGoalReels(): Flow<Int> = flowOf(50)
        override suspend fun getFocusGoalMinutes(): Int = 30
        override suspend fun setFocusGoalMinutes(minutes: Int) {}
        override fun observeFocusGoalMinutes(): Flow<Int> = flowOf(30)
        override suspend fun isAppTracked(appId: String): Boolean = tracked
        override suspend fun setAppTracked(appId: String, tracked: Boolean) { this.tracked = tracked }
    }

    private class TestRuleRepository : RuleRepository {
        val rules = mutableListOf<LimitRule>()
        override suspend fun getRules(): List<LimitRule> = rules
        override suspend fun getRulesForApp(appId: String): List<LimitRule> = rules.filter { it.appIds.isEmpty() || appId in it.appIds }
        override fun observeRules(): Flow<List<LimitRule>> = flowOf(rules)
        override suspend fun saveRule(rule: LimitRule) { rules.add(rule) }
        override suspend fun deleteRule(ruleId: String) { rules.removeAll { it.id == ruleId } }
    }

    private class TestEnforcer : Enforcer {
        val enforced = mutableListOf<Decision>()
        var cleared = false

        override suspend fun enforce(decision: Decision) {
            enforced.add(decision)
        }

        override suspend fun clearEnforcement() {
            cleared = true
        }
    }

    private class TestLiveSurfacePublisher : LiveSurfacePublisher {
        data class Update(val sessionReels: Int, val todayReels: Int, val todayDurationMs: Long, val limitProgress: Float?)
        val updates = mutableListOf<Update>()

        override suspend fun publishSessionUpdate(
            sessionReels: Int,
            todayReels: Int,
            todayDurationMs: Long,
            limitProgress: Float?,
        ) {
            updates.add(Update(sessionReels, todayReels, todayDurationMs, limitProgress))
        }

        override suspend fun endSession() {}
    }

    private class TestNotificationPort : NotificationPort {
        val nudges = mutableListOf<String>()
        val limitReached = mutableListOf<String>()

        override suspend fun showNudge(title: String, body: String) {
            nudges.add(body)
        }

        override suspend fun showLimitReached(title: String, body: String) {
            limitReached.add(body)
        }

        override suspend fun showDetectorBlind(appName: String) {}
        override suspend fun showPermissionRevoked(permissionName: String) {}
    }

    private val clock = TestClock(Instant.parse("2025-01-15T12:00:00Z"))
    private val usageRepo = TestUsageRepository()
    private val settingsRepo = TestSettingsRepository()
    private val ruleRepo = TestRuleRepository()
    private val enforcer = TestEnforcer()
    private val liveSurfacePublisher = TestLiveSurfacePublisher()
    private val notificationPort = TestNotificationPort()
    private val ruleEngine = RuleEngine()

    private val evaluateUsageUseCase = EvaluateUsageUseCase(
        usageRepository = usageRepo,
        ruleRepository = ruleRepo,
        settingsRepository = settingsRepo,
        ruleEngine = ruleEngine,
        clock = clock,
    )

    private val ingestUsageTickUseCase = IngestUsageTickUseCase(
        usageRepository = usageRepo,
        settingsRepository = settingsRepo,
        ruleRepository = ruleRepo,
        evaluateUsageUseCase = evaluateUsageUseCase,
        enforcer = enforcer,
        liveSurfacePublisher = liveSurfacePublisher,
        notificationPort = notificationPort,
        clock = clock,
    )

    @Test
    fun `ingests usage tick and accumulates duration in session and daily usage`() = runTest {
        val tick = UsageTick(
            id = "tick-1",
            appId = "instagram",
            tsUtc = Instant.parse("2025-01-15T12:00:00Z"),
            activeSeconds = 60,
            source = DataSource.ESTIMATED,
        )

        val decision = ingestUsageTickUseCase(tick)

        assertEquals(Decision.Allow, decision)
        assertEquals(1, usageRepo.ticks.size)
        assertNotNull(usageRepo.activeSession)
        assertEquals(60_000L, usageRepo.activeSession?.durationMs)
        assertEquals(DataSource.ESTIMATED, usageRepo.activeSession?.source)

        assertNotNull(usageRepo.dailyUsage)
        assertEquals(60_000L, usageRepo.dailyUsage?.durationMs)
        assertEquals(DataSource.ESTIMATED, usageRepo.dailyUsage?.source)

        assertEquals(1, liveSurfacePublisher.updates.size)
        assertEquals(60_000L, liveSurfacePublisher.updates[0].todayDurationMs)
    }

    @Test
    fun `blocks when daily time limit is exceeded by tick`() = runTest {
        ruleRepo.rules.add(
            LimitRule(
                id = "rule-time",
                appIds = listOf("instagram"),
                kind = RuleKind.DAILY_TIME_LIMIT,
                params = RuleParams(limitMinutes = 1), // 60 seconds limit
                schedule = null,
                strictness = Strictness.HARD_BLOCK,
                enabled = true,
            )
        )

        val tick = UsageTick(
            id = "tick-1",
            appId = "instagram",
            tsUtc = Instant.parse("2025-01-15T12:00:00Z"),
            activeSeconds = 120, // 2 minutes (exceeds 1 minute limit)
            source = DataSource.ESTIMATED,
        )

        val decision = ingestUsageTickUseCase(tick)

        assertTrue(decision is Decision.Block)
        assertEquals(1, enforcer.enforced.size)
        assertEquals(1, notificationPort.limitReached.size)
    }
}
