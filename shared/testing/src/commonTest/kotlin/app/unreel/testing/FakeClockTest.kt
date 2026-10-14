package app.unreel.testing

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeClockTest {

    @Test
    fun `default fake clock starts at expected instant and timezone`() {
        val clock = FakeClock()
        assertEquals(Instant.parse("2025-01-15T12:00:00Z"), clock.now())
        assertEquals(TimeZone.UTC, clock.timeZone())
    }

    @Test
    fun `advancing clock updates now instant correctly`() {
        val clock = FakeClock()
        clock.advanceBy(60_000L) // +1 minute
        assertEquals(Instant.parse("2025-01-15T12:01:00Z"), clock.now())
    }

    @Test
    fun `setting timezone and custom instant updates values`() {
        val clock = FakeClock()
        val customInstant = Instant.parse("2026-10-14T10:00:00Z")
        val customTz = TimeZone.of("Asia/Kolkata")

        clock.setNow(customInstant)
        clock.setTimeZone(customTz)

        assertEquals(customInstant, clock.now())
        assertEquals(customTz, clock.timeZone())
    }
}
