package app.unreel.presentation.home

import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.model.FocusScore
import app.unreel.model.Session

/**
 * UI State for the Home / Dashboard screen.
 */
data class HomeState(
    val isLoading: Boolean = true,
    val todayUsage: DailyUsage? = null,
    val activeSession: Session? = null,
    val focusScore: FocusScore? = null,
    val goalReels: Int = 50,
    val goalMinutes: Int = 30,
    val currentDecision: Decision = Decision.Allow,
    val selectedAppId: String? = null,
    val bypassPassesUsed: Int = 0,
    val errorMessage: String? = null,
) {
    val totalReels: Int get() = todayUsage?.reels ?: 0
    val totalDurationMs: Long get() = todayUsage?.durationMs ?: 0L
    val sessionCount: Int get() = todayUsage?.sessions ?: 0
    val source: DataSource get() = todayUsage?.source ?: DataSource.EXACT
}

/**
 * User intents / actions for the Home screen.
 */
sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data class SelectApp(val appId: String?) : HomeIntent
    data object AcknowledgeNudge : HomeIntent
    data object RequestBypass : HomeIntent
}
