package app.unreel.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for user preferences and configuration.
 */
interface SettingsRepository {
    suspend fun getDayStartHour(): Int
    suspend fun setDayStartHour(hour: Int)
    fun observeDayStartHour(): Flow<Int>

    suspend fun getFocusGoalReels(): Int
    suspend fun setFocusGoalReels(reels: Int)
    fun observeFocusGoalReels(): Flow<Int>

    suspend fun getFocusGoalMinutes(): Int
    suspend fun setFocusGoalMinutes(minutes: Int)
    fun observeFocusGoalMinutes(): Flow<Int>

    suspend fun isAppTracked(appId: String): Boolean
    suspend fun setAppTracked(appId: String, tracked: Boolean)
}
