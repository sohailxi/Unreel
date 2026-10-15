package app.unreel.platform

import app.unreel.model.Decision
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IosPlatformAdaptersTest {

    @Test
    fun testLiveSurfacePublisherSessionLifecycle() = runTest {
        var callbackReceived: IosSurfaceState? = null
        val publisher = IosLiveSurfacePublisher(
            onUpdateCallback = { state -> callbackReceived = state }
        )

        assertEquals(0, publisher.surfaceState.value.sessionReels)
        assertFalse(publisher.surfaceState.value.isSessionActive)

        // Publish session update
        publisher.publishSessionUpdate(
            sessionReels = 7,
            todayReels = 42,
            todayDurationMs = 120_000L,
            limitProgress = 0.5f,
        )

        assertEquals(7, publisher.surfaceState.value.sessionReels)
        assertEquals(42, publisher.surfaceState.value.todayReels)
        assertEquals(120_000L, publisher.surfaceState.value.todayDurationMs)
        assertEquals(0.5f, publisher.surfaceState.value.limitProgress)
        assertTrue(publisher.surfaceState.value.isSessionActive)
        assertEquals(publisher.surfaceState.value, callbackReceived)

        // End session
        publisher.endSession()
        assertEquals(0, publisher.surfaceState.value.sessionReels)
        assertEquals(42, publisher.surfaceState.value.todayReels)
        assertFalse(publisher.surfaceState.value.isSessionActive)
        assertEquals(publisher.surfaceState.value, callbackReceived)
    }

    @Test
    fun testIosEnforcerLifecycle() = runTest {
        var capturedDecision: Decision? = null
        val enforcer = IosEnforcer(
            onDecisionChanged = { capturedDecision = it }
        )

        assertNull(enforcer.currentDecision.value)

        val blockDecision = Decision.Block(
            until = null,
            reason = "Time limit exceeded",
            ruleId = "rule-1",
            canBypass = false,
            passesRemaining = 0,
        )
        enforcer.enforce(blockDecision)

        assertEquals(blockDecision, enforcer.currentDecision.value)
        assertEquals(blockDecision, capturedDecision)

        enforcer.clearEnforcement()
        assertNull(enforcer.currentDecision.value)
        assertNull(capturedDecision)
    }

    @Test
    fun testIosNotificationPortDoesNotThrow() = runTest {
        val notifications = mutableListOf<Pair<String, String>>()
        val portWithSender = IosNotificationPort(
            sender = { title, body -> notifications.add(title to body) }
        )

        portWithSender.showNudge("Nudge", "Take a breath")
        portWithSender.showLimitReached("Limit Reached", "Time to step away")
        portWithSender.showDetectorBlind("Instagram")
        portWithSender.showPermissionRevoked("Screen Time")

        assertEquals(4, notifications.size)
        assertEquals("Nudge" to "Take a breath", notifications[0])
        assertEquals("Limit Reached" to "Time to step away", notifications[1])
        assertEquals("Unreel Alert" to "Tracking is unavailable for Instagram", notifications[2])
        assertEquals("Permission Required" to "Screen Time permission was revoked.", notifications[3])

        // Verify default instance safely no-ops in non-bundle test environment without crashing
        val defaultPort = IosNotificationPort()
        defaultPort.showNudge("Nudge", "Take a breath")
    }
}
