package app.unreel.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.unreel.data.db.Daily_usage_record
import app.unreel.data.db.GetDailyUsageAggregated
import app.unreel.data.db.GetDailyUsageHistoryAggregated
import app.unreel.data.db.GetUsageBreakdownByApp

import app.unreel.data.db.Reel_event
import app.unreel.data.db.Session_record
import app.unreel.data.db.UnreelDatabase
import app.unreel.data.db.Usage_tick
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.ReelEvent
import app.unreel.model.Session
import app.unreel.model.UsageTick
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * SQLDelight-backed implementation of [UsageRepository].
 * Stores raw events and ticks, sessions, and daily usage aggregates.
 */
class SqlDelightUsageRepository(
    private val db: UnreelDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : UsageRepository {

    override suspend fun recordReelEvent(event: ReelEvent) = withContext(ioDispatcher) {
        db.reelEventQueries.insertEvent(
            id = event.id,
            appId = event.appId,
            tsUtc = event.tsUtc.toString(),
            dwellMs = event.dwellMs,
            source = event.source.name,
        )
    }

    override suspend fun recordUsageTick(tick: UsageTick) = withContext(ioDispatcher) {
        db.usageTickQueries.insertTick(
            id = tick.id,
            appId = tick.appId,
            tsUtc = tick.tsUtc.toString(),
            activeSeconds = tick.activeSeconds.toLong(),
            source = tick.source.name,
        )
    }

    override suspend fun getDailyUsage(date: LocalDate, appId: String?): DailyUsage? = withContext(ioDispatcher) {
        val dateStr = date.toString()
        if (appId != null) {
            db.dailyUsageQueries.getDailyUsageForApp(dateStr, appId)
                .executeAsOneOrNull()
                ?.toDomain()
        } else {
            db.dailyUsageQueries.getDailyUsageAggregated(dateStr)
                .executeAsOneOrNull()
                ?.toDomain()
        }
    }

    override fun observeDailyUsage(date: LocalDate, appId: String?): Flow<DailyUsage?> {
        val dateStr = date.toString()
        return if (appId != null) {
            db.dailyUsageQueries.getDailyUsageForApp(dateStr, appId)
                .asFlow()
                .mapToOneOrNull(ioDispatcher)
                .map { it?.toDomain() }
        } else {
            db.dailyUsageQueries.getDailyUsageAggregated(dateStr)
                .asFlow()
                .mapToOneOrNull(ioDispatcher)
                .map { it?.toDomain() }
        }
    }

    override suspend fun saveDailyUsage(dailyUsage: DailyUsage) = withContext(ioDispatcher) {
        db.dailyUsageQueries.upsertDailyUsage(
            localDate = dailyUsage.localDate.toString(),
            appId = dailyUsage.appId,
            reels = dailyUsage.reels.toLong(),
            durationMs = dailyUsage.durationMs,
            sessions = dailyUsage.sessions.toLong(),
            opens = dailyUsage.opens.toLong(),
            source = dailyUsage.source.name,
        )
    }

    override suspend fun getDailyUsageHistory(
        from: LocalDate,
        to: LocalDate,
        appId: String?,
    ): List<DailyUsage> = withContext(ioDispatcher) {
        val fromStr = from.toString()
        val toStr = to.toString()
        if (appId != null) {
            db.dailyUsageQueries.getDailyUsageHistoryForApp(appId, fromStr, toStr)
                .executeAsList()
                .map { it.toDomain() }
        } else {
            db.dailyUsageQueries.getDailyUsageHistoryAggregated(fromStr, toStr)
                .executeAsList()
                .map { it.toDomain() }
        }
    }

    override suspend fun getDailyUsageBreakdown(date: LocalDate): List<DailyUsage> = withContext(ioDispatcher) {
        db.dailyUsageQueries.getDailyUsageBreakdown(date.toString())
            .executeAsList()
            .map { it.toDomain() }
    }

    override fun observeDailyUsageBreakdown(date: LocalDate): Flow<List<DailyUsage>> {
        return db.dailyUsageQueries.getDailyUsageBreakdown(date.toString())
            .asFlow()
            .mapToList(ioDispatcher)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getUsageBreakdownByApp(from: LocalDate, to: LocalDate): List<DailyUsage> = withContext(ioDispatcher) {
        db.dailyUsageQueries.getUsageBreakdownByApp(fromDate = from.toString(), toDate = to.toString())
            .executeAsList()
            .map { it.toDomain(from) }
    }


    override suspend fun getActiveSession(appId: String): Session? = withContext(ioDispatcher) {
        db.sessionQueries.getActiveSession(appId)
            .executeAsOneOrNull()
            ?.toDomain()
    }

    override suspend fun saveSession(session: Session) = withContext(ioDispatcher) {
        db.sessionQueries.insertSession(
            id = session.id,
            appId = session.appId,
            startUtc = session.startUtc.toString(),
            endUtc = session.endUtc.toString(),
            reelCount = session.reelCount.toLong(),
            durationMs = session.durationMs,
            source = session.source.name,
            isActive = 1L,
        )
    }

    override suspend fun endSession(sessionId: String) = withContext(ioDispatcher) {
        db.sessionQueries.endSession(sessionId)
    }

    override suspend fun getRecentSessions(limit: Int): List<Session> = withContext(ioDispatcher) {
        db.sessionQueries.getRecentSessions(limit.toLong())
            .executeAsList()
            .map { it.toDomain() }
    }

    suspend fun getReelEventsForApp(appId: String): List<ReelEvent> = withContext(ioDispatcher) {
        db.reelEventQueries.selectEventsForApp(appId)
            .executeAsList()
            .map { it.toDomain() }
    }

    suspend fun getUsageTicksForApp(appId: String): List<UsageTick> = withContext(ioDispatcher) {
        db.usageTickQueries.selectTicksForApp(appId)
            .executeAsList()
            .map { it.toDomain() }
    }

    private fun Daily_usage_record.toDomain(): DailyUsage = DailyUsage(
        localDate = LocalDate.parse(local_date),
        appId = app_id,
        reels = reels.toInt(),
        durationMs = duration_ms,
        sessions = sessions.toInt(),
        opens = opens.toInt(),
        source = DataSource.valueOf(source),
    )

    private fun GetDailyUsageAggregated.toDomain(): DailyUsage = DailyUsage(
        localDate = LocalDate.parse(local_date),
        appId = app_id,
        reels = reels.toInt(),
        durationMs = duration_ms,
        sessions = sessions.toInt(),
        opens = opens.toInt(),
        source = DataSource.valueOf(source),
    )

    private fun GetDailyUsageHistoryAggregated.toDomain(): DailyUsage = DailyUsage(
        localDate = LocalDate.parse(local_date),
        appId = app_id,
        reels = reels.toInt(),
        durationMs = duration_ms,
        sessions = sessions.toInt(),
        opens = opens.toInt(),
        source = DataSource.valueOf(source),
    )

    private fun GetUsageBreakdownByApp.toDomain(fallbackDate: LocalDate): DailyUsage = DailyUsage(
        localDate = local_date?.let { LocalDate.parse(it) } ?: fallbackDate,
        appId = app_id,
        reels = reels.toInt(),
        durationMs = duration_ms,
        sessions = sessions.toInt(),
        opens = opens.toInt(),
        source = DataSource.valueOf(source),
    )


    private fun Session_record.toDomain(): Session = Session(
        id = id,
        appId = app_id,
        startUtc = Instant.parse(start_utc),
        endUtc = Instant.parse(end_utc),
        reelCount = reel_count.toInt(),
        durationMs = duration_ms,
        source = DataSource.valueOf(source),
    )

    private fun Reel_event.toDomain(): ReelEvent = ReelEvent(
        id = id,
        appId = app_id,
        tsUtc = Instant.parse(ts_utc),
        dwellMs = dwell_ms,
        source = DataSource.valueOf(source),
    )

    private fun Usage_tick.toDomain(): UsageTick = UsageTick(
        id = id,
        appId = app_id,
        tsUtc = Instant.parse(ts_utc),
        activeSeconds = active_seconds.toInt(),
        source = DataSource.valueOf(source),
    )
}
