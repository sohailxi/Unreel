package app.unreel.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unreel.domain.engine.MetricsEngine
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.domain.usecase.EvaluateUsageUseCase
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.model.FocusScore
import app.unreel.time.AppClock
import app.unreel.time.DayBoundary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.toLocalDateTime

/**
 * ViewModel managing Home / Dashboard UI state.
 * Adheres to MVI and exposes SKIE-friendly StateFlow.
 */
class HomeViewModel(
    private val usageRepository: UsageRepository,
    private val settingsRepository: SettingsRepository,
    private val evaluateUsageUseCase: EvaluateUsageUseCase,
    private val metricsEngine: MetricsEngine,
    private val clock: AppClock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var usageObservationJob: Job? = null

    init {
        viewModelScope.launch(dispatcher) {
            combine(
                settingsRepository.observeFocusGoalReels(),
                settingsRepository.observeFocusGoalMinutes(),
            ) { goalReels, goalMinutes ->
                goalReels to goalMinutes
            }.collect { (goalReels, goalMinutes) ->
                _state.update { it.copy(goalReels = goalReels, goalMinutes = goalMinutes) }
                recomputeState()
            }
        }
        refresh()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Refresh -> refresh()
            is HomeIntent.SelectApp -> selectApp(intent.appId)
            is HomeIntent.AcknowledgeNudge -> acknowledgeNudge()
            is HomeIntent.RequestBypass -> requestBypass()
        }
    }

    fun refresh() {
        observeCurrentAppUsage(_state.value.selectedAppId)
    }

    fun selectApp(appId: String?) {
        _state.update { it.copy(selectedAppId = appId, bypassPassesUsed = 0) }
        observeCurrentAppUsage(appId)
    }

    fun acknowledgeNudge() {
        _state.update { it.copy(currentDecision = Decision.Allow) }
    }

    fun requestBypass() {
        viewModelScope.launch(dispatcher) {
            val decision = _state.value.currentDecision
            if (decision is Decision.Block && decision.canBypass) {
                val newBypassCount = _state.value.bypassPassesUsed + 1
                _state.update {
                    it.copy(
                        bypassPassesUsed = newBypassCount,
                        currentDecision = Decision.Allow,
                    )
                }
            }
        }
    }

    internal fun setDecisionForTesting(decision: Decision) {
        _state.update { it.copy(currentDecision = decision) }
    }

    private fun observeCurrentAppUsage(appId: String?) {
        usageObservationJob?.cancel()
        usageObservationJob = viewModelScope.launch(dispatcher) {
            try {
                val now = clock.now()
                val timeZone = clock.timeZone()
                val dayStartHour = settingsRepository.getDayStartHour()
                val dayBoundary = DayBoundary(dayStartHour = dayStartHour)
                val today = dayBoundary.toLocalDate(now, timeZone)

                usageRepository.observeDailyUsage(today, appId)
                    .catch { e ->
                        _state.update { it.copy(errorMessage = e.message, isLoading = false) }
                    }
                    .collect { dailyUsage ->
                        val activeSession = if (appId != null) {
                            usageRepository.getActiveSession(appId)
                        } else {
                            null
                        }

                        val decision = if (appId != null) {
                            evaluateUsageUseCase(appId, bypassPassesUsed = _state.value.bypassPassesUsed)
                        } else {
                            Decision.Allow
                        }

                        val focusScore = calculateScore(dailyUsage, today)

                        _state.update { current ->
                            current.copy(
                                isLoading = false,
                                todayUsage = dailyUsage,
                                activeSession = activeSession,
                                focusScore = focusScore,
                                currentDecision = decision,
                                errorMessage = null,
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    private suspend fun recomputeState() {
        val currentUsage = _state.value.todayUsage
        val now = clock.now()
        val timeZone = clock.timeZone()
        val dayStartHour = settingsRepository.getDayStartHour()
        val dayBoundary = DayBoundary(dayStartHour = dayStartHour)
        val today = dayBoundary.toLocalDate(now, timeZone)

        val focusScore = calculateScore(currentUsage, today)
        _state.update { it.copy(focusScore = focusScore) }
    }

    private suspend fun calculateScore(usage: DailyUsage?, today: kotlinx.datetime.LocalDate): FocusScore {
        val now = clock.now()
        val timeZone = clock.timeZone()
        val dayStartHour = settingsRepository.getDayStartHour()
        val dayBoundary = DayBoundary(dayStartHour = dayStartHour)

        val recentSessions = usageRepository.getRecentSessions(50)
        val todaySessions = recentSessions.filter { session ->
            dayBoundary.toLocalDate(session.startUtc, timeZone) == today &&
                (_state.value.selectedAppId == null || session.appId == _state.value.selectedAppId)
        }

        val longSessionDurationMs = todaySessions
            .filter { it.durationMs >= 20 * 60 * 1000L }
            .sumOf { it.durationMs }

        val lateNightDurationMs = todaySessions
            .filter { session ->
                val hour = session.startUtc.toLocalDateTime(timeZone).hour
                hour >= 23 || hour < 5
            }
            .sumOf { it.durationMs }

        val effectiveUsage = usage ?: DailyUsage(
            localDate = today,
            appId = _state.value.selectedAppId ?: "aggregate",
            reels = 0,
            durationMs = 0,
            sessions = 0,
            opens = 0,
            source = DataSource.EXACT,
        )

        return metricsEngine.calculateFocusScore(
            dailyUsage = effectiveUsage,
            goalReels = _state.value.goalReels,
            goalMinutes = _state.value.goalMinutes,
            lateNightDurationMs = lateNightDurationMs,
            longSessionDurationMs = longSessionDurationMs,
        )
    }
}
