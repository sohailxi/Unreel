package app.unreel.presentation.rules

import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Strictness
import app.unreel.testing.InMemoryRuleRepository
import app.unreel.testing.InMemorySettingsRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RulesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val ruleRepository = InMemoryRuleRepository()
    private val settingsRepository = InMemorySettingsRepository(
        initialDayStartHour = 4,
        initialFocusGoalReels = 50,
        initialFocusGoalMinutes = 30,
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    private fun createViewModel(): RulesViewModel {
        return RulesViewModel(
            ruleRepository = ruleRepository,
            settingsRepository = settingsRepository,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `initial state observes rules and focus goals`() = runTest(testDispatcher) {
        val rule1 = LimitRule(
            id = "rule_1",
            appIds = listOf("com.instagram.android"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 50),
            schedule = null,
            strictness = Strictness.SOFT_BLOCK,
            enabled = true,
        )
        ruleRepository.saveRule(rule1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(1, state.rules.size)
        assertEquals("rule_1", state.rules.first().id)
        assertEquals(50, state.goalReels)
        assertEquals(30, state.goalMinutes)
        assertEquals(4, state.dayStartHour)
    }

    @Test
    fun `toggleRule updates rule enabled status`() = runTest(testDispatcher) {
        val rule1 = LimitRule(
            id = "rule_1",
            appIds = listOf("com.instagram.android"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 50),
            schedule = null,
            strictness = Strictness.SOFT_BLOCK,
            enabled = true,
        )
        ruleRepository.saveRule(rule1)

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(RulesIntent.ToggleRule("rule_1", enabled = false))
        advanceUntilIdle()

        assertFalse(viewModel.state.value.rules.first().enabled)
        assertFalse(ruleRepository.getRules().first().enabled)

        viewModel.onIntent(RulesIntent.ToggleRule("rule_1", enabled = true))
        advanceUntilIdle()

        assertTrue(viewModel.state.value.rules.first().enabled)
        assertTrue(ruleRepository.getRules().first().enabled)
    }

    @Test
    fun `saveRule persists new rule into repository`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val newRule = LimitRule(
            id = "rule_new",
            appIds = listOf("com.zhiliaoapp.musically"),
            kind = RuleKind.DAILY_TIME_LIMIT,
            params = RuleParams(limitMinutes = 45),
            schedule = null,
            strictness = Strictness.HARD_BLOCK,
            enabled = true,
        )

        viewModel.onIntent(RulesIntent.SaveRule(newRule))
        advanceUntilIdle()

        assertEquals(1, viewModel.state.value.rules.size)
        assertEquals("rule_new", viewModel.state.value.rules.first().id)
        assertEquals(1, ruleRepository.getRules().size)
    }

    @Test
    fun `deleteRule removes rule from repository and state`() = runTest(testDispatcher) {
        val rule = LimitRule(
            id = "rule_to_delete",
            appIds = listOf("com.instagram.android"),
            kind = RuleKind.SESSION_CAP,
            params = RuleParams(limitReels = 20),
            schedule = null,
            strictness = Strictness.FRICTION,
            enabled = true,
        )
        ruleRepository.saveRule(rule)

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(1, viewModel.state.value.rules.size)

        viewModel.onIntent(RulesIntent.DeleteRule("rule_to_delete"))
        advanceUntilIdle()

        assertEquals(0, viewModel.state.value.rules.size)
        assertEquals(0, ruleRepository.getRules().size)
    }

    @Test
    fun `updating focus goals and day start hour updates settings`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(RulesIntent.UpdateFocusGoalReels(80))
        viewModel.onIntent(RulesIntent.UpdateFocusGoalMinutes(45))
        viewModel.onIntent(RulesIntent.UpdateDayStartHour(5))
        advanceUntilIdle()

        assertEquals(80, viewModel.state.value.goalReels)
        assertEquals(45, viewModel.state.value.goalMinutes)
        assertEquals(5, viewModel.state.value.dayStartHour)

        assertEquals(80, settingsRepository.getFocusGoalReels())
        assertEquals(45, settingsRepository.getFocusGoalMinutes())
        assertEquals(5, settingsRepository.getDayStartHour())
    }
}
