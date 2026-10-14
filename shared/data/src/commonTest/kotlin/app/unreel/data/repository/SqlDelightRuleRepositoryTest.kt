package app.unreel.data.repository

import app.cash.turbine.test
import app.unreel.data.createTestDatabase
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Schedule
import app.unreel.model.Strictness
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SqlDelightRuleRepositoryTest {

    private lateinit var repository: SqlDelightRuleRepository

    private val rule1 = LimitRule(
        id = "rule-1",
        appIds = listOf("com.instagram.android"),
        kind = RuleKind.DAILY_REEL_LIMIT,
        params = RuleParams(limitReels = 50),
        schedule = Schedule(startTime = "09:00", endTime = "17:00", daysOfWeek = listOf(1, 2, 3, 4, 5)),
        strictness = Strictness.HARD_BLOCK,
        enabled = true,
    )

    private val rule2 = LimitRule(
        id = "rule-2",
        appIds = emptyList(), // applies to all apps
        kind = RuleKind.SESSION_CAP,
        params = RuleParams(limitMinutes = 15, cooldownMinutes = 10),
        schedule = null,
        strictness = Strictness.NUDGE,
        enabled = false,
    )

    @BeforeTest
    fun setUp() {
        val database = createTestDatabase()
        repository = SqlDelightRuleRepository(database)
    }

    @Test
    fun saveAndGetRules() = runTest {
        repository.saveRule(rule1)
        repository.saveRule(rule2)

        val rules = repository.getRules()
        assertEquals(2, rules.size)
        assertTrue(rules.contains(rule1))
        assertTrue(rules.contains(rule2))
    }

    @Test
    fun getRulesForAppFiltersAppropriately() = runTest {
        repository.saveRule(rule1) // com.instagram.android
        repository.saveRule(rule2) // emptyList (all apps)

        val instagramRules = repository.getRulesForApp("com.instagram.android")
        assertEquals(2, instagramRules.size)

        val youtubeRules = repository.getRulesForApp("com.google.android.youtube")
        assertEquals(1, youtubeRules.size)
        assertEquals(rule2, youtubeRules.first())
    }

    @Test
    fun deleteRuleRemovesFromDatabase() = runTest {
        repository.saveRule(rule1)
        repository.saveRule(rule2)

        repository.deleteRule("rule-1")

        val rules = repository.getRules()
        assertEquals(1, rules.size)
        assertEquals(rule2, rules.first())
    }

    @Test
    fun observeRulesEmitsUpdates() = runTest {
        repository.observeRules().test {
            assertEquals(emptyList(), awaitItem())

            repository.saveRule(rule1)
            assertEquals(listOf(rule1), awaitItem())

            repository.saveRule(rule2)
            assertEquals(listOf(rule1, rule2), awaitItem())

            repository.deleteRule("rule-1")
            assertEquals(listOf(rule2), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }
}
