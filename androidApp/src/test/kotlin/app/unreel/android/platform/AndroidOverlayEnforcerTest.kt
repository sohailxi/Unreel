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

    @Test
    fun `enforce Block delegates to overlayController when provided`() = runTest {
        val fakeController = FakeOverlayController()
        val enforcer = AndroidOverlayEnforcer(overlayController = fakeController)

        val blockDecision = Decision.Block(
            until = null,
            reason = "Session limit hit",
            ruleId = "rule-4",
            canBypass = true,
            passesRemaining = 2,
        )

        enforcer.enforce(blockDecision)

        assertTrue(fakeController.isShowing)
        assertEquals("Session limit hit", fakeController.lastReason)
        assertEquals(2, fakeController.lastPassesRemaining)

        enforcer.clearEnforcement()
        assertEquals(false, fakeController.isShowing)
    }

    @Test
    fun `enforce non-Block hides overlay on overlayController`() = runTest {
        val fakeController = FakeOverlayController()
        val enforcer = AndroidOverlayEnforcer(overlayController = fakeController)

        val blockDecision = Decision.Block(
            until = null,
            reason = "Session limit hit",
            ruleId = "rule-4",
            canBypass = false,
            passesRemaining = 0,
        )
        enforcer.enforce(blockDecision)
        assertTrue(fakeController.isShowing)

        enforcer.enforce(Decision.Allow)
        assertEquals(false, fakeController.isShowing)
    }

    private class FakeOverlayController : OverlayController {
        var isShowing: Boolean = false
        var lastReason: String? = null
        var lastPassesRemaining: Int = 0

        override fun showBlockingOverlay(reason: String, passesRemaining: Int, onBypass: (() -> Unit)?) {
            isShowing = true
            lastReason = reason
            lastPassesRemaining = passesRemaining
        }

        override fun hideOverlay() {
            isShowing = false
            lastReason = null
        }

        override fun isOverlayVisible(): Boolean = isShowing
    }
}

