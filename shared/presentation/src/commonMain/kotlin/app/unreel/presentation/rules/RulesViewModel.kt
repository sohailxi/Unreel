package app.unreel.presentation.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.model.LimitRule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing Limit Rules configuration and Focus Goals.
 * Adheres to MVI and exposes SKIE-friendly StateFlow.
 */
class RulesViewModel(
    private val ruleRepository: RuleRepository,
    private val settingsRepository: SettingsRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(RulesState())
    val state: StateFlow<RulesState> = _state.asStateFlow()

    init {
        viewModelScope.launch(dispatcher) {
            combine(
                ruleRepository.observeRules(),
                settingsRepository.observeFocusGoalReels(),
                settingsRepository.observeFocusGoalMinutes(),
                settingsRepository.observeDayStartHour(),
            ) { rules, goalReels, goalMinutes, dayStartHour ->
                RulesState(
                    isLoading = false,
                    rules = rules,
                    goalReels = goalReels,
                    goalMinutes = goalMinutes,
                    dayStartHour = dayStartHour,
                    errorMessage = null,
                )
            }.collect { combinedState ->
                _state.value = combinedState
            }
        }
    }

    fun onIntent(intent: RulesIntent) {
        when (intent) {
            is RulesIntent.LoadRules -> { /* Handled reactively by observeRules */ }
            is RulesIntent.ToggleRule -> toggleRule(intent.ruleId, intent.enabled)
            is RulesIntent.SaveRule -> saveRule(intent.rule)
            is RulesIntent.DeleteRule -> deleteRule(intent.ruleId)
            is RulesIntent.UpdateFocusGoalReels -> updateFocusGoalReels(intent.reels)
            is RulesIntent.UpdateFocusGoalMinutes -> updateFocusGoalMinutes(intent.minutes)
            is RulesIntent.UpdateDayStartHour -> updateDayStartHour(intent.hour)
        }
    }

    fun toggleRule(ruleId: String, enabled: Boolean) {
        viewModelScope.launch(dispatcher) {
            try {
                val rule = _state.value.rules.firstOrNull { it.id == ruleId }
                if (rule != null) {
                    ruleRepository.saveRule(rule.copy(enabled = enabled))
                }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun saveRule(rule: LimitRule) {
        viewModelScope.launch(dispatcher) {
            try {
                ruleRepository.saveRule(rule)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun deleteRule(ruleId: String) {
        viewModelScope.launch(dispatcher) {
            try {
                ruleRepository.deleteRule(ruleId)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun updateFocusGoalReels(reels: Int) {
        viewModelScope.launch(dispatcher) {
            try {
                settingsRepository.setFocusGoalReels(reels)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun updateFocusGoalMinutes(minutes: Int) {
        viewModelScope.launch(dispatcher) {
            try {
                settingsRepository.setFocusGoalMinutes(minutes)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun updateDayStartHour(hour: Int) {
        viewModelScope.launch(dispatcher) {
            try {
                settingsRepository.setDayStartHour(hour)
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message) }
            }
        }
    }
}
