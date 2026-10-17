package app.unreel.android.platform

import app.unreel.data.snapshot.SnapshotPublisher
import app.unreel.data.snapshot.UsageSnapshot
import app.unreel.model.DataSource
import app.unreel.testing.FakeClock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AndroidLiveSurfacePublisherTest {

    private class FakeSnapshotPublisher : SnapshotPublisher {
        var lastPublished: UsageSnapshot? = null

        override suspend fun publishSnapshot(snapshot: UsageSnapshot) {
            lastPublished = snapshot
        }

        override suspend fun readLatestSnapshot(): UsageSnapshot? = lastPublished
    }

    @Test
    fun `publishSessionUpdate updates surfaceState and persists snapshot`() = runTest {
        val fakeClock = FakeClock(currentInstant = Instant.parse("2026-10-15T10:00:00Z"))
        val fakeSnapshotPublisher = FakeSnapshotPublisher()
        val publisher = AndroidLiveSurfacePublisher(
            snapshotPublisher = fakeSnapshotPublisher,
            clock = fakeClock,
        )

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

        val snapshot = fakeSnapshotPublisher.lastPublished
        assertNotNull(snapshot)
        assertEquals(25, snapshot.todayReels)
        assertEquals(120_000L, snapshot.todayDurationMs)
        assertEquals(5, snapshot.sessionReels)
        assertEquals(0.5f, snapshot.limitProgress)
        assertFalse(snapshot.isBlocked)
        assertEquals(DataSource.EXACT, snapshot.source)
        assertEquals("2026-10-15", snapshot.date)
    }

    @Test
    fun `publishSessionUpdate marks blocked when limit reached`() = runTest {
        val fakeClock = FakeClock(currentInstant = Instant.parse("2026-10-15T12:00:00Z"))
        val fakeSnapshotPublisher = FakeSnapshotPublisher()
        val publisher = AndroidLiveSurfacePublisher(
            snapshotPublisher = fakeSnapshotPublisher,
            clock = fakeClock,
        )

        publisher.publishSessionUpdate(
            sessionReels = 15,
            todayReels = 50,
            todayDurationMs = 1_800_000L,
            limitProgress = 1.0f,
        )

        val snapshot = fakeSnapshotPublisher.lastPublished
        assertNotNull(snapshot)
        assertTrue(snapshot.isBlocked)
        assertEquals("Daily limit reached", snapshot.decisionReason)
    }

    @Test
    fun `endSession marks session as inactive and zeroes sessionReels in snapshot`() = runTest {
        val fakeClock = FakeClock(currentInstant = Instant.parse("2026-10-15T10:00:00Z"))
        val fakeSnapshotPublisher = FakeSnapshotPublisher()
        val publisher = AndroidLiveSurfacePublisher(
            snapshotPublisher = fakeSnapshotPublisher,
            clock = fakeClock,
        )

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

        val snapshot = fakeSnapshotPublisher.lastPublished
        assertNotNull(snapshot)
        assertEquals(0, snapshot.sessionReels)
        assertEquals(30, snapshot.todayReels)
        assertEquals(200_000L, snapshot.todayDurationMs)
    }
}
