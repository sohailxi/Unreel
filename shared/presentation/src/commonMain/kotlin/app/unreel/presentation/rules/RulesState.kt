package app.unreel.presentation.rules

import app.unreel.model.LimitRule

/**
 * UI State for the Limit Rules and Settings configuration screen.
 */
data class RulesState(
    val isLoading: Boolean = true,
    val rules: List<LimitRule> = emptyList(),
    val goalReels: Int = 50,
    val goalMinutes: Int = 30,
    val dayStartHour: Int = 4,
    val errorMessage: String? = null,
)

/**
 * User intents / actions for the Limit Rules screen.
 */
sealed interface RulesIntent {
    data object LoadRules : RulesIntent
    data class ToggleRule(val ruleId: String, val enabled: Boolean) : RulesIntent
    data class SaveRule(val rule: LimitRule) : RulesIntent
    data class DeleteRule(val ruleId: String) : RulesIntent
    data class UpdateFocusGoalReels(val reels: Int) : RulesIntent
    data class UpdateFocusGoalMinutes(val minutes: Int) : RulesIntent
    data class UpdateDayStartHour(val hour: Int) : RulesIntent
}
