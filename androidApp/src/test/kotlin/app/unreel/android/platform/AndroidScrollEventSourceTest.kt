package app.unreel.android.platform

import app.unreel.model.DataSource
import app.unreel.model.ReelEvent
import app.unreel.testing.FakeClock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidScrollEventSourceTest {

    @Test
    fun `emit sends ReelEvent to events flow`() = runTest {
        val eventSource = AndroidScrollEventSource()
        val clock = FakeClock()
        val now = clock.now()

        val event = ReelEvent(
            id = "reel-1",
            appId = "com.instagram.android",
            tsUtc = now,
            dwellMs = 2000L,
            source = DataSource.EXACT,
        )

        eventSource.emit(event)
        val received = eventSource.events.first()

        assertEquals("reel-1", received.id)
        assertEquals("com.instagram.android", received.appId)
        assertEquals(DataSource.EXACT, received.source)
    }
}
