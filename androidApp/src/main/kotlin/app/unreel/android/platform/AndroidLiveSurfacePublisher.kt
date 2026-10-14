package app.unreel.android.platform

import app.unreel.domain.port.LiveSurfacePublisher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State representing live surfaces (floating overlay counter, Glance widgets).
 */
data class SurfaceState(
    val sessionReels: Int = 0,
    val todayReels: Int = 0,
    val todayDurationMs: Long = 0L,
    val limitProgress: Float? = null,
    val isSessionActive: Boolean = false,
)

/**
 * Android implementation of [LiveSurfacePublisher].
 * Updates floating overlay counter and Glance widgets with current session/daily metrics.
 */
class AndroidLiveSurfacePublisher : LiveSurfacePublisher {
    private val _surfaceState = MutableStateFlow(SurfaceState())
    val surfaceState: StateFlow<SurfaceState> = _surfaceState.asStateFlow()

    override suspend fun publishSessionUpdate(
        sessionReels: Int,
        todayReels: Int,
        todayDurationMs: Long,
        limitProgress: Float?,
    ) {
        _surfaceState.value = SurfaceState(
            sessionReels = sessionReels,
            todayReels = todayReels,
            todayDurationMs = todayDurationMs,
            limitProgress = limitProgress,
            isSessionActive = true,
        )
    }

    override suspend fun endSession() {
        _surfaceState.value = _surfaceState.value.copy(
            isSessionActive = false,
            sessionReels = 0,
        )
    }
}
