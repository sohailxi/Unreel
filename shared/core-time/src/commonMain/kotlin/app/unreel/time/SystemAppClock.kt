package app.unreel.time

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

/**
 * Production implementation of [AppClock] using system clock and system default timezone.
 */
class SystemAppClock : AppClock {
    override fun now(): Instant = Clock.System.now()
    override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
}
