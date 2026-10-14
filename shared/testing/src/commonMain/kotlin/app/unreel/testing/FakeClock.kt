package app.unreel.testing

import app.unreel.time.AppClock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/**
 * A deterministic clock for testing. Tests can advance time and change timezone.
 */
class FakeClock(
    private var currentInstant: Instant = Instant.parse("2025-01-15T12:00:00Z"),
    private var currentTimeZone: TimeZone = TimeZone.UTC,
) : AppClock {

    override fun now(): Instant = currentInstant

    override fun timeZone(): TimeZone = currentTimeZone

    fun advanceBy(durationMs: Long) {
        currentInstant = Instant.fromEpochMilliseconds(
            currentInstant.toEpochMilliseconds() + durationMs,
        )
    }

    fun setNow(instant: Instant) {
        currentInstant = instant
    }

    fun setTimeZone(tz: TimeZone) {
        currentTimeZone = tz
    }
}
