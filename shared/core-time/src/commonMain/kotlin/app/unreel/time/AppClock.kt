package app.unreel.time

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/**
 * Port for clock access. Inject this everywhere instead of using Clock.System directly.
 * Allows deterministic testing with FakeClock.
 */
interface AppClock {
    fun now(): Instant
    fun timeZone(): TimeZone
}
