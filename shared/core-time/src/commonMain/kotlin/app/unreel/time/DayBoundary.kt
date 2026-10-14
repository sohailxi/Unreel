package app.unreel.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Calculates logical day boundaries based on a configurable cut-off time.
 * For example, with dayStartHour = 4, any time between 00:00 and 03:59 belongs
 * to the previous calendar day's logical usage cycle.
 */
data class DayBoundary(
    val dayStartHour: Int = 4,
    val dayStartMinute: Int = 0,
) {
    init {
        require(dayStartHour in 0..23) { "dayStartHour must be between 0 and 23" }
        require(dayStartMinute in 0..59) { "dayStartMinute must be between 0 and 59" }
    }

    private val cutoffTime: LocalTime = LocalTime(dayStartHour, dayStartMinute)

    /**
     * Determines which logical [LocalDate] an [Instant] belongs to, evaluated in [timeZone].
     */
    fun toLocalDate(instant: Instant, timeZone: TimeZone): LocalDate {
        val ldt = instant.toLocalDateTime(timeZone)
        return if (ldt.time < cutoffTime) {
            ldt.date.minus(1, DateTimeUnit.DAY)
        } else {
            ldt.date
        }
    }

    /**
     * Calculates the inclusive start [Instant] of the logical day for [date] in [timeZone].
     */
    fun startOf(date: LocalDate, timeZone: TimeZone): Instant {
        val ldt = LocalDateTime(date, cutoffTime)
        return ldt.toInstant(timeZone)
    }

    /**
     * Calculates the exclusive end [Instant] of the logical day for [date] in [timeZone].
     */
    fun endOf(date: LocalDate, timeZone: TimeZone): Instant {
        val nextDate = date.plus(1, DateTimeUnit.DAY)
        val ldt = LocalDateTime(nextDate, cutoffTime)
        return ldt.toInstant(timeZone)
    }

    /**
     * Returns the [DayInterval] representing [startOf, endOf) for [date] in [timeZone].
     */
    fun dayInterval(date: LocalDate, timeZone: TimeZone): DayInterval {
        return DayInterval(
            start = startOf(date, timeZone),
            endExclusive = endOf(date, timeZone),
        )
    }
}

/**
 * A time interval representing a logical day [start, endExclusive).
 */
data class DayInterval(
    val start: Instant,
    val endExclusive: Instant,
) {
    fun contains(instant: Instant): Boolean {
        return instant in start..<endExclusive
    }
}
