package app.unreel.data.repository

import app.cash.turbine.test
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryImplTest {

    private lateinit var settings: MapSettings
    private lateinit var repository: SettingsRepositoryImpl

    @BeforeTest
    fun setUp() {
        settings = MapSettings()
        repository = SettingsRepositoryImpl(settings)
    }

    @Test
    fun dayStartHourDefaultsTo4AndCanBeUpdated() = runTest {
        assertEquals(4, repository.getDayStartHour())

        repository.setDayStartHour(5)
        assertEquals(5, repository.getDayStartHour())
    }

    @Test
    fun observeDayStartHourEmitsUpdates() = runTest {
        repository.observeDayStartHour().test {
            assertEquals(4, awaitItem())

            repository.setDayStartHour(6)
            assertEquals(6, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun focusGoalReelsDefaultsTo50AndCanBeUpdated() = runTest {
        assertEquals(50, repository.getFocusGoalReels())

        repository.setFocusGoalReels(30)
        assertEquals(30, repository.getFocusGoalReels())
    }

    @Test
    fun observeFocusGoalReelsEmitsUpdates() = runTest {
        repository.observeFocusGoalReels().test {
            assertEquals(50, awaitItem())

            repository.setFocusGoalReels(40)
            assertEquals(40, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun focusGoalMinutesDefaultsTo30AndCanBeUpdated() = runTest {
        assertEquals(30, repository.getFocusGoalMinutes())

        repository.setFocusGoalMinutes(45)
        assertEquals(45, repository.getFocusGoalMinutes())
    }

    @Test
    fun observeFocusGoalMinutesEmitsUpdates() = runTest {
        repository.observeFocusGoalMinutes().test {
            assertEquals(30, awaitItem())

            repository.setFocusGoalMinutes(60)
            assertEquals(60, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun appTrackedState() = runTest {
        assertTrue(repository.isAppTracked("com.instagram.android"))

        repository.setAppTracked("com.instagram.android", false)
        assertFalse(repository.isAppTracked("com.instagram.android"))

        repository.setAppTracked("com.instagram.android", true)
        assertTrue(repository.isAppTracked("com.instagram.android"))
    }
}
