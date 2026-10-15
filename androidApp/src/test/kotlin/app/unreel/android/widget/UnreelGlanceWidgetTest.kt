package app.unreel.android.widget

import app.unreel.model.DataSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UnreelGlanceWidgetTest {

    @Test
    fun `formatGlanceTime formats durations compactly for home screen widget`() {
        assertEquals("0m", formatGlanceTime(0L))
        assertEquals("2m", formatGlanceTime(120_000L))
        assertEquals("45m", formatGlanceTime(2_700_000L))
        assertEquals("1h 15m", formatGlanceTime(4_500_000L))
        assertEquals("2h", formatGlanceTime(7_200_000L))
    }

    @Test
    fun `parseSnapshot returns UsageSnapshot from valid json`() {
        val validJson = """
            {
                "version": 1,
                "date": "2026-10-15",
                "todayReels": 42,
                "todayDurationMs": 1800000,
                "sessionReels": 8,
                "activeSessionDurationMs": 300000,
                "limitProgress": 0.7,
                "decisionReason": null,
                "isBlocked": false,
                "source": "EXACT",
                "updatedAtUtc": "2026-10-15T10:00:00Z"
            }
        """.trimIndent()

        val snapshot = parseSnapshot(validJson)
        assertNotNull(snapshot)
        assertEquals(42, snapshot.todayReels)
        assertEquals(1800000L, snapshot.todayDurationMs)
        assertEquals(8, snapshot.sessionReels)
        assertEquals(0.7f, snapshot.limitProgress)
        assertFalse(snapshot.isBlocked)
        assertEquals(DataSource.EXACT, snapshot.source)
    }

    @Test
    fun `parseSnapshot returns null on null or corrupt input`() {
        assertNull(parseSnapshot(null))
        assertNull(parseSnapshot(""))
        assertNull(parseSnapshot("{ invalid json }"))
    }

    @Test
    fun `createWidgetState maps snapshot accurately`() {
        val validJson = """
            {
                "version": 1,
                "date": "2026-10-15",
                "todayReels": 55,
                "todayDurationMs": 3600000,
                "sessionReels": 12,
                "activeSessionDurationMs": 600000,
                "limitProgress": 1.0,
                "decisionReason": "Daily reel limit reached",
                "isBlocked": true,
                "source": "EXACT",
                "updatedAtUtc": "2026-10-15T11:00:00Z"
            }
        """.trimIndent()

        val snapshot = parseSnapshot(validJson)
        val state = createWidgetState(snapshot)

        assertEquals(55, state.todayReels)
        assertEquals("1h", state.formattedDuration)
        assertEquals(12, state.sessionReels)
        assertTrue(state.isSessionActive)
        assertEquals(1.0f, state.limitProgress)
        assertTrue(state.isBlocked)
        assertEquals("Daily reel limit reached", state.statusText)
    }

    @Test
    fun `createWidgetState provides safe defaults when snapshot is null`() {
        val state = createWidgetState(null)

        assertEquals(0, state.todayReels)
        assertEquals("0m", state.formattedDuration)
        assertEquals(0, state.sessionReels)
        assertFalse(state.isSessionActive)
        assertNull(state.limitProgress)
        assertFalse(state.isBlocked)
        assertEquals("Within limits", state.statusText)
    }

    @Test
    fun `receiver exposes UnreelGlanceWidget instance`() {
        val receiver = UnreelGlanceWidgetReceiver()
        assertNotNull(receiver.glanceAppWidget)
        assertTrue(receiver.glanceAppWidget is UnreelGlanceWidget)
    }
}
