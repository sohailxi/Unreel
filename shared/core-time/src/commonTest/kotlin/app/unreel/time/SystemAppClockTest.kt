package app.unreel.time

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

class SystemAppClockTest {

    @Test
    fun `SystemAppClock returns current time and system timezone`() {
        val clock = SystemAppClock()
        val before = Clock.System.now()
        val now = clock.now()
        val after = Clock.System.now()

        assertTrue(now >= before - 1.seconds)
        assertTrue(now <= after + 1.seconds)
        assertNotNull(clock.timeZone())
        val defaultTz = TimeZone.currentSystemDefault()
        assertNotNull(defaultTz)
    }
}
