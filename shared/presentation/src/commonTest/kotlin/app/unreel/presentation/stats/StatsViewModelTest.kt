package app.unreel.presentation.stats

import app.unreel.domain.engine.MetricsEngine
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.TrendDirection
import app.unreel.testing.FakeClock
import app.unreel.testing.InMemorySettingsRepository
import app.unreel.testing.InMemoryUsageRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val clock = FakeClock(Instant.parse("2026-10-14T12:00:00Z"))
    private val usageRepository = InMemoryUsageRepository()
    private val settingsRepository = InMemorySettingsRepository(initialDayStartHour = 4)
    private val metricsEngine = MetricsEngine()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    private fun createViewModel(): StatsViewModel {
        return StatsViewModel(
            usageRepository = usageRepository,
            settingsRepository = settingsRepository,
            metricsEngine = metricsEngine,
            clock = clock,
            dispatcher = testDispatcher,
        )
    }

    @Test
    fun `initial state loads 7 days of usage and calculates totals and averages`() = runTest(testDispatcher) {
        // Pre-populate 7 days of data (Oct 8 to Oct 14)
        for (i in 0 until 7) {
            val date = LocalDate(2026, 10, 8 + i)
            usageRepository.saveDailyUsage(
                DailyUsage(
                    localDate = date,
                    appId = "com.instagram.android",
                    reels = 20 * (i + 1), // 20, 40, 60, 80, 100, 120, 140 -> trend slipping
                    durationMs = 600_000L * (i + 1),
                    sessions = 2,
                    opens = 3,
                    source = DataSource.EXACT,
                ),
            )
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(7, state.selectedDays)
        assertEquals(7, state.dailyHistory.size)
        // Total reels = 20 + 40 + 60 + 80 + 100 + 120 + 140 = 560
        assertEquals(560, state.totalReels)
        // Average daily reels = 560 / 7 = 80
        assertEquals(80, state.averageDailyReels)
        // Trend should be slipping because usage is strictly increasing
        assertEquals(TrendDirection.SLIPPING, state.trend.direction)
        assertEquals(DataSource.EXACT, state.dataSource)
    }

    @Test
    fun `selecting 14 days time range reloads historical data for 14 days`() = runTest(testDispatcher) {
        for (day in 1..14) {
            val date = LocalDate(2026, 10, day)
            usageRepository.saveDailyUsage(
                DailyUsage(
                    localDate = date,
                    appId = "com.zhiliaoapp.musically",
                    reels = 10,
                    durationMs = 300_000L,
                    sessions = 1,
                    opens = 2,
                    source = DataSource.ESTIMATED,
                ),
            )
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(StatsIntent.SelectTimeRange(14))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(14, state.selectedDays)
        assertEquals(14, state.dailyHistory.size)
        assertEquals(140, state.totalReels)
        assertEquals(10, state.averageDailyReels)
        assertEquals(DataSource.ESTIMATED, state.dataSource)
    }

    @Test
    fun `selecting specific app filters stats by app id`() = runTest(testDispatcher) {
        val today = LocalDate(2026, 10, 14)
        val ig = "com.instagram.android"
        val tt = "com.zhiliaoapp.musically"

        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = ig,
                reels = 100,
                durationMs = 3_000_000L,
                sessions = 5,
                opens = 5,
                source = DataSource.EXACT,
            ),
        )
        usageRepository.saveDailyUsage(
            DailyUsage(
                localDate = today,
                appId = tt,
                reels = 15,
                durationMs = 500_000L,
                sessions = 1,
                opens = 2,
                source = DataSource.EXACT,
            ),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onIntent(StatsIntent.SelectApp(tt))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(tt, state.selectedAppId)
        assertEquals(1, state.dailyHistory.size)
        assertEquals(15, state.totalReels)
    }

    @Test
    fun `appBreakdown in stats populates cumulative usage per app for selected time range`() = runTest(testDispatcher) {
        val day1 = LocalDate(2026, 10, 13)
        val day2 = LocalDate(2026, 10, 14)
        val ig = "com.instagram.android"
        val yt = "com.google.android.youtube"

        usageRepository.saveDailyUsage(
            DailyUsage(day1, ig, 30, 900_000L, 2, 2, DataSource.EXACT),
        )
        usageRepository.saveDailyUsage(
            DailyUsage(day2, ig, 40, 1_200_000L, 2, 2, DataSource.EXACT),
        )
        usageRepository.saveDailyUsage(
            DailyUsage(day2, yt, 25, 750_000L, 1, 1, DataSource.EXACT),
        )

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.appBreakdown.size)
        // Instagram has 30 + 40 = 70 reels
        assertEquals(ig, state.appBreakdown[0].appId)
        assertEquals(70, state.appBreakdown[0].reels)
        // YouTube has 25 reels
        assertEquals(yt, state.appBreakdown[1].appId)
        assertEquals(25, state.appBreakdown[1].reels)
    }
}

