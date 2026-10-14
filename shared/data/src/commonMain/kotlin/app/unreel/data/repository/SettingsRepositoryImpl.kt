package app.unreel.data.repository

import app.unreel.domain.repository.SettingsRepository
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * [SettingsRepository] implementation backed by [multiplatform-settings].
 */
class SettingsRepositoryImpl(
    private val settings: Settings,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : SettingsRepository {

    private val dayStartHourFlow = MutableStateFlow(
        settings.getInt(KEY_DAY_START_HOUR, DEFAULT_DAY_START_HOUR),
    )
    private val focusGoalReelsFlow = MutableStateFlow(
        settings.getInt(KEY_FOCUS_GOAL_REELS, DEFAULT_FOCUS_GOAL_REELS),
    )
    private val focusGoalMinutesFlow = MutableStateFlow(
        settings.getInt(KEY_FOCUS_GOAL_MINUTES, DEFAULT_FOCUS_GOAL_MINUTES),
    )

    override suspend fun getDayStartHour(): Int = withContext(ioDispatcher) {
        settings.getInt(KEY_DAY_START_HOUR, DEFAULT_DAY_START_HOUR)
    }

    override suspend fun setDayStartHour(hour: Int) = withContext(ioDispatcher) {
        settings.putInt(KEY_DAY_START_HOUR, hour)
        dayStartHourFlow.value = hour
    }

    override fun observeDayStartHour(): Flow<Int> = dayStartHourFlow.asStateFlow()

    override suspend fun getFocusGoalReels(): Int = withContext(ioDispatcher) {
        settings.getInt(KEY_FOCUS_GOAL_REELS, DEFAULT_FOCUS_GOAL_REELS)
    }

    override suspend fun setFocusGoalReels(reels: Int) = withContext(ioDispatcher) {
        settings.putInt(KEY_FOCUS_GOAL_REELS, reels)
        focusGoalReelsFlow.value = reels
    }

    override fun observeFocusGoalReels(): Flow<Int> = focusGoalReelsFlow.asStateFlow()

    override suspend fun getFocusGoalMinutes(): Int = withContext(ioDispatcher) {
        settings.getInt(KEY_FOCUS_GOAL_MINUTES, DEFAULT_FOCUS_GOAL_MINUTES)
    }

    override suspend fun setFocusGoalMinutes(minutes: Int) = withContext(ioDispatcher) {
        settings.putInt(KEY_FOCUS_GOAL_MINUTES, minutes)
        focusGoalMinutesFlow.value = minutes
    }

    override fun observeFocusGoalMinutes(): Flow<Int> = focusGoalMinutesFlow.asStateFlow()

    override suspend fun isAppTracked(appId: String): Boolean = withContext(ioDispatcher) {
        settings.getBoolean(KEY_TRACKED_APP_PREFIX + appId, true)
    }

    override suspend fun setAppTracked(appId: String, tracked: Boolean) = withContext(ioDispatcher) {
        settings.putBoolean(KEY_TRACKED_APP_PREFIX + appId, tracked)
    }

    companion object {
        const val KEY_DAY_START_HOUR = "day_start_hour"
        const val KEY_FOCUS_GOAL_REELS = "focus_goal_reels"
        const val KEY_FOCUS_GOAL_MINUTES = "focus_goal_minutes"
        const val KEY_TRACKED_APP_PREFIX = "tracked_app_"

        const val DEFAULT_DAY_START_HOUR = 4
        const val DEFAULT_FOCUS_GOAL_REELS = 50
        const val DEFAULT_FOCUS_GOAL_MINUTES = 30
    }
}
