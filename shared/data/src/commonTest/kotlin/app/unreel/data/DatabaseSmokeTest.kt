package app.unreel.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DatabaseSmokeTest {

    @Test
    fun databaseCanBeInstantiatedAndQueried() {
        val database = createTestDatabase()
        assertNotNull(database)

        database.reelEventQueries.insertEvent(
            id = "event-1",
            appId = "com.instagram.android",
            tsUtc = "2026-10-14T10:00:00Z",
            dwellMs = 4500L,
            source = "EXACT",
        )

        val events = database.reelEventQueries.selectEventsForApp("com.instagram.android").executeAsList()
        assertEquals(1, events.size)
        assertEquals("event-1", events[0].id)
        assertEquals(4500L, events[0].dwell_ms)
        assertEquals("EXACT", events[0].source)
    }
}
