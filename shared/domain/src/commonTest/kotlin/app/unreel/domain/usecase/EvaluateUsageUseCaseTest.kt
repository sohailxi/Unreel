package app.unreel.domain.usecase

import app.unreel.domain.engine.RuleEngine
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
import app.unreel.time.DayBoundary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluateUsageUseCaseTest {

    private val fixedInstant = Instant.parse("2025-01-15T12:00:00Z")
    private val clock = object : AppClock {
        override fun now(): Instant = fixedInstant
        override fun timeZone(): TimeZone = TimeZone.UTC
    }

    private class InMemoryUsageRepository : UsageRepository {
        var dailyUsage: DailyUsage? = null
        var activeSession: Session? = null

        override suspend fun recordReelEvent(event: ReelEvent) {}
        override suspend fun recordUsageTick(tick: UsageTick) {}
        override suspend fun getDailyUsage(date: LocalDate, appId: String?): DailyUsage? = dailyUsage
        override fun observeDailyUsage(date: LocalDate, appId: String?): Flow<DailyUsage?> = flowOf(dailyUsage)
        override suspend fun saveDailyUsage(dailyUsage: DailyUsage) { this.dailyUsage = dailyUsage }
        override suspend fun getDailyUsageHistory(from: LocalDate, to: LocalDate, appId: String?): List<DailyUsage> = emptyList()
        override suspend fun getActiveSession(appId: String): Session? = activeSession
        override suspend fun saveSession(session: Session) { activeSession = session }
        override suspend fun endSession(sessionId: String) { activeSession = null }
        override suspend fun getRecentSessions(limit: Int): List<Session> = emptyList()
    }

    private class InMemoryRuleRepository : RuleRepository {
        var rules = mutableListOf<LimitRule>()
        override suspend fun getRules(): List<LimitRule> = rules
        override suspend fun getRulesForApp(appId: String): List<LimitRule> = rules.filter { it.appIds.isEmpty() || appId in it.appIds }
        override fun observeRules(): Flow<List<LimitRule>> = flowOf(rules)
        override suspend fun saveRule(rule: LimitRule) { rules.add(rule) }
        override suspend fun deleteRule(ruleId: String) { rules.removeAll { it.id == ruleId } }
    }

    private class InMemorySettingsRepository : SettingsRepository {
        var dayStartHour: Int = 4
        override suspend fun getDayStartHour(): Int = dayStartHour
        override suspend fun setDayStartHour(hour: Int) { dayStartHour = hour }
        override fun observeDayStartHour(): Flow<Int> = flowOf(dayStartHour)
        override suspend fun getFocusGoalReels(): Int = 50
        override suspend fun setFocusGoalReels(reels: Int) {}
        override fun observeFocusGoalReels(): Flow<Int> = flowOf(50)
        override suspend fun getFocusGoalMinutes(): Int = 30
        override suspend fun setFocusGoalMinutes(minutes: Int) {}
        override fun observeFocusGoalMinutes(): Flow<Int> = flowOf(30)
        override suspend fun isAppTracked(appId: String): Boolean = true
        override suspend fun setAppTracked(appId: String, tracked: Boolean) {}
    }

    private val usageRepo = InMemoryUsageRepository()
    private val ruleRepo = InMemoryRuleRepository()
    private val settingsRepo = InMemorySettingsRepository()
    private val ruleEngine = RuleEngine()

    private val useCase = EvaluateUsageUseCase(
        usageRepository = usageRepo,
        ruleRepository = ruleRepo,
        settingsRepository = settingsRepo,
        ruleEngine = ruleEngine,
        clock = clock,
    )

    @Test
    fun `returns Allow when no rules are configured`() = runTest {
        val decision = useCase("instagram")
        assertEquals(Decision.Allow, decision)
    }

    @Test
    fun `evaluates daily reel limit and returns Block when exceeded`() = runTest {
        ruleRepo.rules.add(
            LimitRule(
                id = "rule-1",
                appIds = listOf("instagram"),
                kind = RuleKind.DAILY_REEL_LIMIT,
                params = RuleParams(limitReels = 100),
                schedule = null,
                strictness = Strictness.HARD_BLOCK,
                enabled = true,
            )
        )
        usageRepo.dailyUsage = DailyUsage(
            localDate = LocalDate(2025, 1, 15),
            appId = "instagram",
            reels = 105,
            durationMs = 200_000L,
            sessions = 3,
            opens = 3,
            source = DataSource.EXACT,
        )

        val decision = useCase("instagram")
        assertTrue(decision is Decision.Block)
        assertEquals("rule-1", decision.ruleId)
    }
}
