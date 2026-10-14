package app.unreel.android.platform

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AndroidLiveSurfacePublisherTest {

    @Test
    fun `publishSessionUpdate updates surfaceState`() = runTest {
        val publisher = AndroidLiveSurfacePublisher()

        publisher.publishSessionUpdate(
            sessionReels = 5,
            todayReels = 25,
            todayDurationMs = 120_000L,
            limitProgress = 0.5f,
        )

        val state = publisher.surfaceState.value
        assertEquals(5, state.sessionReels)
        assertEquals(25, state.todayReels)
        assertEquals(120_000L, state.todayDurationMs)
        assertEquals(0.5f, state.limitProgress)
        assertEquals(true, state.isSessionActive)
    }

    @Test
    fun `endSession marks session as inactive`() = runTest {
        val publisher = AndroidLiveSurfacePublisher()
        publisher.publishSessionUpdate(
            sessionReels = 10,
            todayReels = 30,
            todayDurationMs = 200_000L,
            limitProgress = 0.8f,
        )

        publisher.endSession()

        val state = publisher.surfaceState.value
        assertEquals(false, state.isSessionActive)
        assertEquals(0, state.sessionReels)
    }
}
