package app.unreel.time

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DayBoundaryTest {

    private val timeZone = TimeZone.UTC

    @Test
    fun `with 4am cutoff 03_59_59 belongs to previous day`() {
        val boundary = DayBoundary(dayStartHour = 4, dayStartMinute = 0)
        // 2025-01-15T03:59:59Z -> belongs to 2025-01-14
        val instant = Instant.parse("2025-01-15T03:59:59Z")
        val localDate = boundary.toLocalDate(instant, timeZone)
        assertEquals(LocalDate(2025, 1, 14), localDate)
    }

    @Test
    fun `with 4am cutoff 04_00_00 belongs to current day`() {
        val boundary = DayBoundary(dayStartHour = 4, dayStartMinute = 0)
        val instant = Instant.parse("2025-01-15T04:00:00Z")
        val localDate = boundary.toLocalDate(instant, timeZone)
        assertEquals(LocalDate(2025, 1, 15), localDate)
    }

    @Test
    fun `with midnight cutoff maps directly to calendar date`() {
        val boundary = DayBoundary(dayStartHour = 0, dayStartMinute = 0)
        val instant = Instant.parse("2025-01-15T00:00:00Z")
        val localDate = boundary.toLocalDate(instant, timeZone)
        assertEquals(LocalDate(2025, 1, 15), localDate)

        val beforeMidnight = Instant.parse("2025-01-14T23:59:59Z")
        assertEquals(LocalDate(2025, 1, 14), boundary.toLocalDate(beforeMidnight, timeZone))
    }

    @Test
    fun `startOf and endOf compute exact boundary instants`() {
        val boundary = DayBoundary(dayStartHour = 4, dayStartMinute = 0)
        val targetDate = LocalDate(2025, 1, 15)

        val start = boundary.startOf(targetDate, timeZone)
        val end = boundary.endOf(targetDate, timeZone)

        assertEquals(Instant.parse("2025-01-15T04:00:00Z"), start)
        assertEquals(Instant.parse("2025-01-16T04:00:00Z"), end)
    }

    @Test
    fun `day interval containment check`() {
        val boundary = DayBoundary(dayStartHour = 4, dayStartMinute = 0)
        val targetDate = LocalDate(2025, 1, 15)
        val interval = boundary.dayInterval(targetDate, timeZone)

        assertTrue(interval.contains(Instant.parse("2025-01-15T04:00:00Z")))
        assertTrue(interval.contains(Instant.parse("2025-01-15T23:59:59Z")))
        assertTrue(interval.contains(Instant.parse("2025-01-16T03:59:59Z")))
        assertFalse(interval.contains(Instant.parse("2025-01-15T03:59:59Z")))
        assertFalse(interval.contains(Instant.parse("2025-01-16T04:00:00Z")))
    }

    @Test
    fun `works across non-UTC time zones`() {
        val boundary = DayBoundary(dayStartHour = 4, dayStartMinute = 0)
        val tokyo = TimeZone.of("Asia/Tokyo") // UTC+9
        // 2025-01-15 03:30 Tokyo time is 2025-01-14 18:30 UTC -> belongs to 2025-01-14 in Tokyo
        val instantBeforeCutoff = Instant.parse("2025-01-14T18:30:00Z")
        assertEquals(LocalDate(2025, 1, 14), boundary.toLocalDate(instantBeforeCutoff, tokyo))

        // 2025-01-15 04:30 Tokyo time is 2025-01-14 19:30 UTC -> belongs to 2025-01-15 in Tokyo
        val instantAfterCutoff = Instant.parse("2025-01-14T19:30:00Z")
        assertEquals(LocalDate(2025, 1, 15), boundary.toLocalDate(instantAfterCutoff, tokyo))
    }

    @Test
    fun `property test - instant to localDate is always within startOf and endOf`() = runTest {
        val boundary = DayBoundary(dayStartHour = 4, dayStartMinute = 0)
        val baseEpochSeconds = 1735689600L // 2025-01-01T00:00:00Z

        checkAll(100, Arb.int(0, 86400 * 365)) { offsetSeconds ->
            val instant = Instant.fromEpochSeconds(baseEpochSeconds + offsetSeconds)
            val date = boundary.toLocalDate(instant, timeZone)
            val start = boundary.startOf(date, timeZone)
            val end = boundary.endOf(date, timeZone)

            assertTrue(instant >= start, "Instant $instant must be >= startOf $start for date $date")
            assertTrue(instant < end, "Instant $instant must be < endOf $end for date $date")
        }
    }
}
