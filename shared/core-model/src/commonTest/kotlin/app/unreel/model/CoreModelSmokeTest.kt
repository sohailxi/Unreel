package app.unreel.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Smoke tests proving the core model compiles and runs on both JVM and iOS targets.
 */
class CoreModelSmokeTest {

    @Test
    fun `DataSource enum has both variants`() {
        assertEquals(2, DataSource.entries.size)
        assertNotEquals(DataSource.EXACT, DataSource.ESTIMATED)
    }

    @Test
    fun `TrackedApp can be created with all fields`() {
        val app = TrackedApp(
            id = "instagram",
            platform = Platform.ANDROID,
            displayName = "Instagram",
            packageOrTokenRef = "com.instagram.android",
            category = "social",
        )
        assertEquals("instagram", app.id)
        assertEquals(Platform.ANDROID, app.platform)
    }

    @Test
    fun `Decision sealed hierarchy is exhaustive`() {
        val decisions: List<Decision> = listOf(
            Decision.Allow,
            Decision.Nudge(reason = "Over limit", ruleId = "rule-1"),
            Decision.Friction(
                kind = FrictionKind.BREATHING_PAUSE,
                reason = "Take a break",
                ruleId = "rule-2",
            ),
            Decision.Block(
                until = null,
                reason = "Daily limit reached",
                ruleId = "rule-3",
                canBypass = false,
                passesRemaining = 0,
            ),
        )
        assertEquals(4, decisions.size)
    }

    @Test
    fun `TrendResult can represent all directions`() {
        val improving = TrendResult(TrendDirection.IMPROVING, -10.0, Confidence.HIGH)
        val steady = TrendResult(TrendDirection.STEADY, 2.0, Confidence.MEDIUM)
        val slipping = TrendResult(TrendDirection.SLIPPING, 15.0, Confidence.HIGH)
        assertEquals(TrendDirection.IMPROVING, improving.direction)
        assertEquals(TrendDirection.STEADY, steady.direction)
        assertEquals(TrendDirection.SLIPPING, slipping.direction)
    }
}
