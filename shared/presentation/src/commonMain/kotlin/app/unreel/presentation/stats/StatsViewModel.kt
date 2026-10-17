package app.unreel.presentation.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.unreel.domain.engine.MetricsEngine
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.DataSource
import app.unreel.time.AppClock
import app.unreel.time.DayBoundary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

/**
 * ViewModel managing Statistics and Trends UI state.
 * Adheres to MVI and exposes SKIE-friendly StateFlow.
 */
class StatsViewModel(
    private val usageRepository: UsageRepository,
    private val settingsRepository: SettingsRepository,
    private val metricsEngine: MetricsEngine,
    private val clock: AppClock,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        loadStats(_state.value.selectedDays, _state.value.selectedAppId)
    }

    fun onIntent(intent: StatsIntent) {
        when (intent) {
            is StatsIntent.SelectTimeRange -> selectTimeRange(intent.days)
            is StatsIntent.SelectApp -> selectApp(intent.appId)
            is StatsIntent.Refresh -> refresh()
        }
    }

    fun selectTimeRange(days: Int) {
        _state.update { it.copy(selectedDays = days) }
        loadStats(days, _state.value.selectedAppId)
    }

    fun selectApp(appId: String?) {
        _state.update { it.copy(selectedAppId = appId) }
        loadStats(_state.value.selectedDays, appId)
    }

    fun refresh() {
        loadStats(_state.value.selectedDays, _state.value.selectedAppId)
    }

    private fun loadStats(days: Int, appId: String?) {
        viewModelScope.launch(dispatcher) {
            _state.update { it.copy(isLoading = true) }
            try {
                val now = clock.now()
                val timeZone = clock.timeZone()
                val dayStartHour = settingsRepository.getDayStartHour()
                val dayBoundary = DayBoundary(dayStartHour = dayStartHour)
                val today = dayBoundary.toLocalDate(now, timeZone)
                val from = today.minus(days - 1, DateTimeUnit.DAY)

                val history = usageRepository.getDailyUsageHistory(from = from, to = today, appId = appId)
                val rawBreakdown = usageRepository.getUsageBreakdownByApp(from = from, to = today)
                val filteredBreakdown = if (appId != null) {
                    rawBreakdown.filter { it.appId == appId }
                } else {
                    rawBreakdown
                }
                val trend = metricsEngine.calculateTrend(history.map { it.reels.toLong() })
                val totalReels = history.sumOf { it.reels }
                val totalDurationMs = history.sumOf { it.durationMs }
                val avgReels = if (days > 0) totalReels / days else 0
                val avgDurationMs = if (days > 0) totalDurationMs / days else 0L
                val dataSource = history.firstOrNull()?.source ?: DataSource.EXACT

                _state.update {
                    it.copy(
                        isLoading = false,
                        selectedDays = days,
                        selectedAppId = appId,
                        dailyHistory = history,
                        appBreakdown = filteredBreakdown,
                        trend = trend,
                        totalReels = totalReels,
                        totalDurationMs = totalDurationMs,
                        averageDailyReels = avgReels,
                        averageDailyDurationMs = avgDurationMs,
                        dataSource = dataSource,
                        errorMessage = null,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
