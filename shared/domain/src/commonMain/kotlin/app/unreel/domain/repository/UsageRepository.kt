package app.unreel.domain.repository

import app.unreel.model.DailyUsage
import app.unreel.model.ReelEvent
import app.unreel.model.Session
import app.unreel.model.UsageTick
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/**
 * Repository interface for raw events, sessions, and daily aggregates.
 */
interface UsageRepository {
    suspend fun recordReelEvent(event: ReelEvent)
    suspend fun recordUsageTick(tick: UsageTick)

    suspend fun getDailyUsage(date: LocalDate, appId: String? = null): DailyUsage?
    fun observeDailyUsage(date: LocalDate, appId: String? = null): Flow<DailyUsage?>

    suspend fun getDailyUsageHistory(from: LocalDate, to: LocalDate, appId: String? = null): List<DailyUsage>

    suspend fun getActiveSession(appId: String): Session?
    suspend fun saveSession(session: Session)
    suspend fun endSession(sessionId: String)
    suspend fun getRecentSessions(limit: Int = 20): List<Session>
}
