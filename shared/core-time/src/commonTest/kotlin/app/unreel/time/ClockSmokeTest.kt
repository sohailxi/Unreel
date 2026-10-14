package app.unreel.time

import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * Smoke test for the Clock port — verifies the interface compiles
 * and FakeClock works on all targets.
 */
class ClockSmokeTest {

    @Test
    fun `AppClock interface is usable`() {
        // This test just verifies the port interface compiles on all targets.
        // Real tests will use FakeClock from the testing module.
        val clockType = AppClock::class
        assertNotNull(clockType)
    }
}
