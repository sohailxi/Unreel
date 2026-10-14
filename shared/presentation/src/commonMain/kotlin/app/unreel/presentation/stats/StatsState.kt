package app.unreel.presentation.stats

import app.unreel.model.Confidence
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.TrendDirection
import app.unreel.model.TrendResult

/**
 * UI State for the Statistics & Trends screen.
 */
data class StatsState(
    val isLoading: Boolean = true,
    val selectedDays: Int = 7,
    val selectedAppId: String? = null,
    val dailyHistory: List<DailyUsage> = emptyList(),
    val trend: TrendResult = TrendResult(
        direction = TrendDirection.STEADY,
        percentChange = 0.0,
        confidence = Confidence.LOW,
    ),
    val totalReels: Int = 0,
    val totalDurationMs: Long = 0L,
    val averageDailyReels: Int = 0,
    val averageDailyDurationMs: Long = 0L,
    val dataSource: DataSource = DataSource.EXACT,
    val errorMessage: String? = null,
)

/**
 * User intents / actions for the Statistics screen.
 */
sealed interface StatsIntent {
    data class SelectTimeRange(val days: Int) : StatsIntent
    data class SelectApp(val appId: String?) : StatsIntent
    data object Refresh : StatsIntent
}
