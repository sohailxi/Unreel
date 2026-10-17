package app.unreel.data.repository

import app.cash.turbine.test
import app.unreel.data.createTestDatabase
import app.unreel.model.DataSource
import app.unreel.model.DailyUsage
import app.unreel.model.ReelEvent
import app.unreel.model.Session
import app.unreel.model.UsageTick
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SqlDelightUsageRepositoryTest {

    private lateinit var repository: SqlDelightUsageRepository
    private val now = Instant.parse("2026-10-14T10:00:00Z")
    private val today = LocalDate(2026, 10, 14)

    @BeforeTest
    fun setUp() {
        val database = createTestDatabase()
        repository = SqlDelightUsageRepository(database)
    }

    @Test
    fun recordReelEventPersistsEvent() = runTest {
        val event = ReelEvent(
            id = "event-1",
            appId = "com.instagram.android",
            tsUtc = now,
            dwellMs = 4000L,
            source = DataSource.EXACT,
        )

        repository.recordReelEvent(event)

        val retrieved = repository.getReelEventsForApp("com.instagram.android")
        assertEquals(1, retrieved.size)
        assertEquals(event, retrieved.first())
    }

    @Test
    fun recordUsageTickPersistsTick() = runTest {
        val tick = UsageTick(
            id = "tick-1",
            appId = "ph.telegra.Telegraph",
            tsUtc = now,
            activeSeconds = 60,
            source = DataSource.ESTIMATED,
        )

        repository.recordUsageTick(tick)

        val retrieved = repository.getUsageTicksForApp("ph.telegra.Telegraph")
        assertEquals(1, retrieved.size)
        assertEquals(tick, retrieved.first())
    }

    @Test
    fun saveAndGetDailyUsageForSpecificApp() = runTest {
        val usage = DailyUsage(
            localDate = today,
            appId = "com.instagram.android",
            reels = 25,
            durationMs = 120_000L,
            sessions = 3,
            opens = 4,
            source = DataSource.EXACT,
        )

        repository.saveDailyUsage(usage)

        val retrieved = repository.getDailyUsage(today, "com.instagram.android")
        assertEquals(usage, retrieved)
    }

    @Test
    fun getDailyUsageAggregatedAcrossApps() = runTest {
        val usage1 = DailyUsage(
            localDate = today,
            appId = "com.instagram.android",
            reels = 20,
            durationMs = 100_000L,
            sessions = 2,
            opens = 2,
            source = DataSource.EXACT,
        )
        val usage2 = DailyUsage(
            localDate = today,
            appId = "com.google.android.youtube",
            reels = 30,
            durationMs = 150_000L,
            sessions = 3,
            opens = 3,
            source = DataSource.EXACT,
        )

        repository.saveDailyUsage(usage1)
        repository.saveDailyUsage(usage2)

        val aggregated = repository.getDailyUsage(today, appId = null)
        assertNotNull(aggregated)
        assertEquals(today, aggregated.localDate)
        assertEquals(50, aggregated.reels)
        assertEquals(250_000L, aggregated.durationMs)
        assertEquals(5, aggregated.sessions)
        assertEquals(5, aggregated.opens)
    }

    @Test
    fun observeDailyUsageEmitsUpdates() = runTest {
        val usage = DailyUsage(
            localDate = today,
            appId = "com.instagram.android",
            reels = 10,
            durationMs = 60_000L,
            sessions = 1,
            opens = 1,
            source = DataSource.EXACT,
        )

        repository.observeDailyUsage(today, "com.instagram.android").test {
            assertNull(awaitItem())

            repository.saveDailyUsage(usage)
            assertEquals(usage, awaitItem())

            val updated = usage.copy(reels = 15)
            repository.saveDailyUsage(updated)
            assertEquals(updated, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getDailyUsageHistoryReturnsOrderedEntries() = runTest {
        val day1 = LocalDate(2026, 10, 12)
        val day2 = LocalDate(2026, 10, 13)
        val day3 = LocalDate(2026, 10, 14)

        repository.saveDailyUsage(DailyUsage(day1, "app1", 5, 20_000L, 1, 1, DataSource.EXACT))
        repository.saveDailyUsage(DailyUsage(day2, "app1", 10, 40_000L, 2, 2, DataSource.EXACT))
        repository.saveDailyUsage(DailyUsage(day3, "app1", 15, 60_000L, 3, 3, DataSource.EXACT))

        val history = repository.getDailyUsageHistory(day1, day2, "app1")
        assertEquals(2, history.size)
        assertEquals(day1, history[0].localDate)
        assertEquals(day2, history[1].localDate)
    }

    @Test
    fun getDailyUsageBreakdownReturnsAppsOrderedByReelsDescending() = runTest {
        val usage1 = DailyUsage(
            localDate = today,
            appId = "com.instagram.android",
            reels = 20,
            durationMs = 100_000L,
            sessions = 2,
            opens = 2,
            source = DataSource.EXACT,
        )
        val usage2 = DailyUsage(
            localDate = today,
            appId = "com.google.android.youtube",
            reels = 45,
            durationMs = 250_000L,
            sessions = 3,
            opens = 3,
            source = DataSource.EXACT,
        )

        repository.saveDailyUsage(usage1)
        repository.saveDailyUsage(usage2)

        val breakdown = repository.getDailyUsageBreakdown(today)
        assertEquals(2, breakdown.size)
        assertEquals("com.google.android.youtube", breakdown[0].appId)
        assertEquals(45, breakdown[0].reels)
        assertEquals("com.instagram.android", breakdown[1].appId)
        assertEquals(20, breakdown[1].reels)
    }

    @Test
    fun observeDailyUsageBreakdownEmitsUpdates() = runTest {
        val usage = DailyUsage(
            localDate = today,
            appId = "com.instagram.android",
            reels = 10,
            durationMs = 50_000L,
            sessions = 1,
            opens = 1,
            source = DataSource.EXACT,
        )

        repository.observeDailyUsageBreakdown(today).test {
            assertEquals(emptyList(), awaitItem())

            repository.saveDailyUsage(usage)
            val updatedList = awaitItem()
            assertEquals(1, updatedList.size)
            assertEquals("com.instagram.android", updatedList[0].appId)
            assertEquals(10, updatedList[0].reels)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getUsageBreakdownByAppAggregatesAcrossDates() = runTest {
        val day1 = LocalDate(2026, 10, 12)
        val day2 = LocalDate(2026, 10, 13)

        repository.saveDailyUsage(DailyUsage(day1, "com.instagram.android", 10, 50_000L, 1, 1, DataSource.EXACT))
        repository.saveDailyUsage(DailyUsage(day2, "com.instagram.android", 20, 100_000L, 2, 2, DataSource.EXACT))
        repository.saveDailyUsage(DailyUsage(day1, "com.google.android.youtube", 5, 25_000L, 1, 1, DataSource.EXACT))

        val breakdown = repository.getUsageBreakdownByApp(day1, day2)
        assertEquals(2, breakdown.size)
        assertEquals("com.instagram.android", breakdown[0].appId)
        assertEquals(30, breakdown[0].reels)
        assertEquals(150_000L, breakdown[0].durationMs)
        assertEquals("com.google.android.youtube", breakdown[1].appId)
        assertEquals(5, breakdown[1].reels)
    }


    @Test
    fun sessionLifecycleTracking() = runTest {
        val session = Session(
            id = "sess-1",
            appId = "com.instagram.android",
            startUtc = now,
            endUtc = now.plus(kotlin.time.Duration.parse("5m")),
            reelCount = 12,
            durationMs = 300_000L,
            source = DataSource.EXACT,
        )

        repository.saveSession(session)

        val active = repository.getActiveSession("com.instagram.android")
        assertNotNull(active)
        assertEquals(session, active)

        repository.endSession("sess-1")

        val activeAfterEnd = repository.getActiveSession("com.instagram.android")
        assertNull(activeAfterEnd)

        val recent = repository.getRecentSessions(10)
        assertEquals(1, recent.size)
        assertEquals(session, recent[0])
    }
}
