package app.unreel.data.snapshot

import app.unreel.model.DataSource
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SnapshotPublisherTest {

    private lateinit var sink: SettingsSnapshotSink
    private lateinit var publisher: SnapshotPublisherImpl

    @BeforeTest
    fun setUp() {
        val settings = MapSettings()
        sink = SettingsSnapshotSink(settings)
        publisher = SnapshotPublisherImpl(sink)
    }

    @Test
    fun publishAndReadSnapshot() = runTest {
        val snapshot = UsageSnapshot(
            version = 1,
            date = "2026-10-14",
            todayReels = 42,
            todayDurationMs = 180_000L,
            sessionReels = 15,
            activeSessionDurationMs = 60_000L,
            limitProgress = 0.84f,
            decisionReason = "Daily limit nearing",
            isBlocked = false,
            source = DataSource.EXACT,
            updatedAtUtc = "2026-10-14T10:15:00Z",
        )

        publisher.publishSnapshot(snapshot)

        val retrieved = publisher.readLatestSnapshot()
        assertNotNull(retrieved)
        assertEquals(snapshot, retrieved)
    }

    @Test
    fun readLatestSnapshotReturnsNullWhenEmpty() = runTest {
        assertNull(publisher.readLatestSnapshot())
    }

    @Test
    fun readLatestSnapshotReturnsNullOnCorruptedData() = runTest {
        sink.writeSnapshotJson("{ invalid json payload }")
        assertNull(publisher.readLatestSnapshot())
    }

    @Test
    fun snapshotContainsVersionAndEstimatedSource() = runTest {
        val snapshot = UsageSnapshot(
            version = 1,
            date = "2026-10-14",
            todayReels = 10,
            todayDurationMs = 50_000L,
            sessionReels = 0,
            activeSessionDurationMs = 0L,
            limitProgress = null,
            decisionReason = null,
            isBlocked = true,
            source = DataSource.ESTIMATED,
            updatedAtUtc = "2026-10-14T10:20:00Z",
        )

        publisher.publishSnapshot(snapshot)

        val rawJson = sink.readSnapshotJson()
        assertNotNull(rawJson)
        assertTrue(rawJson.contains("\"source\":\"ESTIMATED\""))
        assertTrue(rawJson.contains("\"isBlocked\":true"))
    }
}
