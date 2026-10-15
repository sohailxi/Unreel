package app.unreel.android.ui

import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Strictness
import kotlin.test.Test
import kotlin.test.assertEquals

class RulesScreenTest {

    @Test
    fun `formatRuleKind returns readable titles`() {
        assertEquals("Daily Reel Limit", formatRuleKind(RuleKind.DAILY_REEL_LIMIT))
        assertEquals("Daily Time Limit", formatRuleKind(RuleKind.DAILY_TIME_LIMIT))
        assertEquals("Session Cap", formatRuleKind(RuleKind.SESSION_CAP))
        assertEquals("Schedule Window", formatRuleKind(RuleKind.SCHEDULE_WINDOW))
    }

    @Test
    fun `formatRuleParamsSummary formats limit values accurately`() {
        val reelRule = LimitRule(
            id = "r1",
            appIds = listOf("instagram"),
            kind = RuleKind.DAILY_REEL_LIMIT,
            params = RuleParams(limitReels = 60),
            schedule = null,
            strictness = Strictness.SOFT_BLOCK,
            enabled = true,
        )
        assertEquals("60 reels limit", formatRuleParamsSummary(reelRule))

        val timeRule = LimitRule(
            id = "r2",
            appIds = listOf("youtube"),
            kind = RuleKind.DAILY_TIME_LIMIT,
            params = RuleParams(limitMinutes = 45),
            schedule = null,
            strictness = Strictness.FRICTION,
            enabled = true,
        )
        assertEquals("45 minutes limit", formatRuleParamsSummary(timeRule))
    }

    @Test
    fun `RulesScreen rule kinds mapping covers all RuleKind values`() {
        RuleKind.entries.forEach { kind ->
            val title = formatRuleKind(kind)
            kotlin.test.assertTrue(title.isNotEmpty())
            kotlin.test.assertNotNull(ruleKindIcon(kind))
        }
    }

    @Test
    fun `requiresOverlayShield returns true for blocking and friction strictness levels`() {
        kotlin.test.assertFalse(requiresOverlayShield(Strictness.NUDGE))
        kotlin.test.assertTrue(requiresOverlayShield(Strictness.FRICTION))
        kotlin.test.assertTrue(requiresOverlayShield(Strictness.SOFT_BLOCK))
        kotlin.test.assertTrue(requiresOverlayShield(Strictness.HARD_BLOCK))
        kotlin.test.assertTrue(requiresOverlayShield(Strictness.STRICT))
    }
}
