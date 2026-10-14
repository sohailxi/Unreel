package app.unreel.android.platform

import app.unreel.model.Decision
import app.unreel.model.FrictionKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AndroidOverlayEnforcerTest {

    @Test
    fun `enforce Block sets currentDecision to Block and isBlocked to true`() = runTest {
        val enforcer = AndroidOverlayEnforcer()
        val blockDecision = Decision.Block(
            until = null,
            reason = "Daily limit reached",
            ruleId = "rule-1",
            canBypass = true,
            passesRemaining = 1,
        )

        enforcer.enforce(blockDecision)

        assertEquals(blockDecision, enforcer.activeDecision.value)
        assertTrue(enforcer.isEnforcing.value)
    }

    @Test
    fun `enforce Friction sets activeDecision to Friction`() = runTest {
        val enforcer = AndroidOverlayEnforcer()
        val frictionDecision = Decision.Friction(
            kind = FrictionKind.BREATHING_PAUSE,
            reason = "Take a breath",
            ruleId = "rule-2",
        )

        enforcer.enforce(frictionDecision)

        assertEquals(frictionDecision, enforcer.activeDecision.value)
        assertTrue(enforcer.isEnforcing.value)
    }

    @Test
    fun `clearEnforcement resets activeDecision to Allow`() = runTest {
        val enforcer = AndroidOverlayEnforcer()
        val blockDecision = Decision.Block(
            until = null,
            reason = "Session limit reached",
            ruleId = "rule-3",
            canBypass = false,
            passesRemaining = 0,
        )
        enforcer.enforce(blockDecision)
        assertTrue(enforcer.isEnforcing.value)

        enforcer.clearEnforcement()

        assertEquals(Decision.Allow, enforcer.activeDecision.value)
        assertEquals(false, enforcer.isEnforcing.value)
    }
}
