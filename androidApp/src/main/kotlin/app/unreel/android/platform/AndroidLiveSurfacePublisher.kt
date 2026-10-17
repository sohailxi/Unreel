package app.unreel.android.platform

import android.content.Context
import androidx.glance.appwidget.updateAll
import app.unreel.android.widget.UnreelGlanceWidget
import app.unreel.data.snapshot.SettingsSnapshotSink
import app.unreel.data.snapshot.SnapshotPublisher
import app.unreel.data.snapshot.UsageSnapshot
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.model.DataSource
import app.unreel.time.AppClock
import app.unreel.time.SystemAppClock
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
class AndroidLiveSurfacePublisher(
    private val context: Context? = null,
    private val snapshotPublisher: SnapshotPublisher? = null,
    private val clock: AppClock = SystemAppClock(),
    private val logger: Logger = Logger.withTag("AndroidLiveSurfacePublisher"),
) : LiveSurfacePublisher {
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
        saveSnapshot(
            todayReels = todayReels,
            todayDurationMs = todayDurationMs,
            sessionReels = sessionReels,
            limitProgress = limitProgress,
        )
        updateGlanceWidgets()
    }

    override suspend fun endSession() {
        val current = _surfaceState.value
        _surfaceState.value = current.copy(
            isSessionActive = false,
            sessionReels = 0,
        )
        saveSnapshot(
            todayReels = current.todayReels,
            todayDurationMs = current.todayDurationMs,
            sessionReels = 0,
            limitProgress = current.limitProgress,
        )
        updateGlanceWidgets()
    }

    private suspend fun saveSnapshot(
        todayReels: Int,
        todayDurationMs: Long,
        sessionReels: Int,
        limitProgress: Float?,
    ) {
        val now = clock.now()
        val dateStr = now.toLocalDateTime(clock.timeZone()).date.toString()
        val isBlocked = limitProgress != null && limitProgress >= 1.0f
        val decisionReason = if (isBlocked) "Daily limit reached" else null

        val snapshot = UsageSnapshot(
            version = 1,
            date = dateStr,
            todayReels = todayReels,
            todayDurationMs = todayDurationMs,
            sessionReels = sessionReels,
            activeSessionDurationMs = 0L,
            limitProgress = limitProgress,
            decisionReason = decisionReason,
            isBlocked = isBlocked,
            source = DataSource.EXACT,
            updatedAtUtc = now.toString(),
        )

        try {
            if (snapshotPublisher != null) {
                snapshotPublisher.publishSnapshot(snapshot)
            } else if (context != null) {
                val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
                val prefs = context.getSharedPreferences("unreel_preferences", Context.MODE_PRIVATE)
                prefs.edit().putString(SettingsSnapshotSink.KEY_USAGE_SNAPSHOT, json.encodeToString(snapshot)).apply()
            }
        } catch (t: Throwable) {
            logger.w(t) { "Failed to persist snapshot for Glance widget" }
        }
    }

    private suspend fun updateGlanceWidgets() {
        val ctx = context ?: return
        try {
            UnreelGlanceWidget().updateAll(ctx)
        } catch (t: Throwable) {
            logger.w(t) { "Failed to update Glance widgets" }
        }
    }
}
