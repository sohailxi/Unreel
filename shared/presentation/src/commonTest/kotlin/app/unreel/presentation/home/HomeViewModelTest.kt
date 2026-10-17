package app.unreel.presentation.home

import app.unreel.domain.engine.MetricsEngine
import app.unreel.domain.engine.RuleEngine
import app.unreel.domain.usecase.EvaluateUsageUseCase
import app.unreel.model.Confidence
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Schedule
import app.unreel.model.Strictness
import app.unreel.model.Session
import app.unreel.testing.FakeClock
import app.unreel.testing.InMemoryRuleRepository
import app.unreel.testing.InMemorySettingsRepository
import app.unreel.testing.InMemoryUsageRepository
import app.unreel.time.DayBoundary
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val clock = FakeClock(Instant.parse("2026-10-14T10:00:00Z"))
    private val usageRepository = InMemoryUsageRepository()
    private val ruleRepository = InMemoryRuleRepository()
    private val settingsRepository = InMemorySettingsRepository(
        initialDayStartHour = 4,
        initialFocusGoalReels = 60,
        initialFocusGoalMinutes = 30,
    )
    private val ruleEngine = RuleEngine()
    private val metricsEngine = MetricsEngine()
    private lateinit var evaluateUsageUseCase: EvaluateUsageUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        evaluateUsageUseCase = EvaluateUsageUseCase(
            usageRepository = usageRepository,
            ruleRepository = ruleRepository,
            settingsRepository = settingsRepository,
            ruleEngine = ruleEngine,
            clock = clock,
        )
    }

    private fun createViewModel(): HomeViewModel {
        return HomeViewModel(
            usageRepository = usageRepository,
            settingsRepository = settingsRepository,
            evaluateUsageUseCase = evaluateUsageUseCase,
            metricsEngine = metricsEngine,
            clock = clock,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `initial state loads goals and usage successfully`() = runTest(testDispatcher) {
        val today = LocalDate(2026, 10, 14)
        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = "com.instagram.android",
                reels = 30,
                durationMs = 900_000L, // 15 mins
                sessions = 2,
                opens = 3,
                source = DataSource.EXACT,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(30, state.totalReels)
        assertEquals(900_000L, state.totalDurationMs)
        assertEquals(60, state.goalReels)
        assertEquals(30, state.goalMinutes)
        assertNotNull(state.focusScore)
        assertEquals(DataSource.EXACT, state.source)
    }

    @Test
    fun `observing today usage populates appBreakdown ordered by reels descending`() = runTest(testDispatcher) {
        val today = LocalDate(2026, 10, 14)
        val ig = "com.instagram.android"
        val yt = "com.google.android.youtube"

        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = ig,
                reels = 20,
                durationMs = 600_000L,
                sessions = 2,
                opens = 2,
                source = DataSource.EXACT,
            ),
        )
        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = yt,
                reels = 45,
                durationMs = 1_200_000L,
                sessions = 3,
                opens = 3,
                source = DataSource.EXACT,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.appBreakdown.size)
        assertEquals(yt, state.appBreakdown[0].appId)
        assertEquals(45, state.appBreakdown[0].reels)
        assertEquals(ig, state.appBreakdown[1].appId)
        assertEquals(20, state.appBreakdown[1].reels)

        // Now filter by Instagram
        viewModel.onIntent(HomeIntent.SelectApp(ig))
        advanceUntilIdle()

        val filteredState = viewModel.state.value
        assertEquals(1, filteredState.appBreakdown.size)
        assertEquals(ig, filteredState.appBreakdown[0].appId)
        assertEquals(20, filteredState.appBreakdown[0].reels)
    }


    @Test
    fun `selecting specific app loads that app usage and evaluates limit rules`() = runTest(testDispatcher) {
        val today = LocalDate(2026, 10, 14)
        val igAppId = "com.instagram.android"
        val ttAppId = "com.zhiliaoapp.musically"

        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = igAppId,
                reels = 100,
                durationMs = 3_600_000L,
                sessions = 5,
                opens = 5,
                source = DataSource.EXACT,
            ),
        )
        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = ttAppId,
                reels = 10,
                durationMs = 300_000L,
                sessions = 1,
                opens = 1,
                source = DataSource.EXACT,
            ),
        )

        // Add a daily reel limit rule for Instagram: max 50 reels
        ruleRepository.saveRule(
            LimitRule(
                id = "rule_ig_daily",
                appIds = listOf(igAppId),
                kind = RuleKind.DAILY_REEL_LIMIT,
                params = RuleParams(limitReels = 50),
                schedule = null,
                strictness = Strictness.HARD_BLOCK,
                enabled = true,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        // Select Instagram
        viewModel.onIntent(HomeIntent.SelectApp(igAppId))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(igAppId, state.selectedAppId)
        assertEquals(100, state.totalReels)
        // Should evaluate to Block since 100 > 50 with HARD_BLOCK strictness
        val decision = state.currentDecision
        assertEquals(true, decision is Decision.Block)
    }

    @Test
    fun `requestBypass increments bypass count and re-evaluates usage`() = runTest(testDispatcher) {
        val today = LocalDate(2026, 10, 14)
        val igAppId = "com.instagram.android"

        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = igAppId,
                reels = 100,
                durationMs = 3_600_000L,
                sessions = 5,
                opens = 5,
                source = DataSource.EXACT,
            ),
        )

        // Soft block rule with 1 bypass allowed
        ruleRepository.saveRule(
            LimitRule(
                id = "rule_ig_mod",
                appIds = listOf(igAppId),
                kind = RuleKind.DAILY_REEL_LIMIT,
                params = RuleParams(limitReels = 50, maxPasses = 1),
                schedule = null,
                strictness = Strictness.SOFT_BLOCK,
                enabled = true,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.SelectApp(igAppId))
        advanceUntilIdle()

        // Initially blocked
        val initialDecision = viewModel.state.value.currentDecision
        assertEquals(true, initialDecision is Decision.Block)

        // Request bypass
        viewModel.onIntent(HomeIntent.RequestBypass)
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.bypassPassesUsed)
        assertEquals(Decision.Allow, viewModel.state.value.currentDecision)
    }

    @Test
    fun `acknowledgeNudge sets decision to Allow`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.setDecisionForTesting(Decision.Nudge("Take a breather", "rule_test"))
        assertEquals(true, viewModel.state.value.currentDecision is Decision.Nudge)

        viewModel.onIntent(HomeIntent.AcknowledgeNudge)
        assertEquals(Decision.Allow, viewModel.state.value.currentDecision)
    }
}
