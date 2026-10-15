package app.unreel.android.service

import app.unreel.android.platform.SurfaceState
import app.unreel.model.Decision
import app.unreel.model.FrictionKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnreelForegroundServiceTest {

    @Test
    fun `formatDuration formats duration correctly for ongoing notification`() {
        assertEquals("0m", UnreelForegroundService.formatDuration(0L))
        assertEquals("2m", UnreelForegroundService.formatDuration(120_000L))
        assertEquals("45m", UnreelForegroundService.formatDuration(2_700_000L))
        assertEquals("1h 15m", UnreelForegroundService.formatDuration(4_500_000L))
        assertEquals("2h", UnreelForegroundService.formatDuration(7_200_000L))
    }

    @Test
    fun `getNotificationTitle returns correct title for decisions and progress`() {
        val block = Decision.Block(
            until = null,
            reason = "Daily limit reached",
            ruleId = "rule-1",
            canBypass = false,
            passesRemaining = 0,
        )
        assertEquals(
            "⚠️ Reel Limit Reached",
            UnreelForegroundService.getNotificationTitle(decision = block, limitProgress = 1.0f),
        )

        val friction = Decision.Friction(
            kind = FrictionKind.BREATHING_PAUSE,
            reason = "Take a breath",
            ruleId = "rule-2",
        )
        assertEquals(
            "⏳ Pause & Breathe",
            UnreelForegroundService.getNotificationTitle(decision = friction, limitProgress = 0.9f),
        )

        assertEquals(
            "⚡ Nearing Daily Limit",
            UnreelForegroundService.getNotificationTitle(decision = Decision.Allow, limitProgress = 0.88f),
        )

        assertEquals(
            "Unreel Active",
            UnreelForegroundService.getNotificationTitle(decision = Decision.Allow, limitProgress = 0.5f),
        )
    }

    @Test
    fun `getNotificationContent builds descriptive text for surface state`() {
        val normalState = SurfaceState(
            sessionReels = 4,
            todayReels = 24,
            todayDurationMs = 1_800_000L,
            limitProgress = 0.48f,
            isSessionActive = true,
        )
        val content = UnreelForegroundService.getNotificationContent(
            surfaceState = normalState,
            decision = Decision.Allow,
        )
        assertTrue(content.contains("24 reels today"))
        assertTrue(content.contains("30m"))
        assertTrue(content.contains("4 in session"))

        val block = Decision.Block(
            until = null,
            reason = "Maximum scroll time exceeded",
            ruleId = "rule-3",
            canBypass = false,
            passesRemaining = 0,
        )
        val blockedContent = UnreelForegroundService.getNotificationContent(
            surfaceState = normalState,
            decision = block,
        )
        assertEquals("Maximum scroll time exceeded", blockedContent)
    }

    @Test
    fun `action constants are correctly defined`() {
        assertEquals("app.unreel.android.action.START_MONITORING", UnreelForegroundService.ACTION_START)
        assertEquals("app.unreel.android.action.STOP_MONITORING", UnreelForegroundService.ACTION_STOP)
        assertEquals("unreel_monitor_channel", UnreelForegroundService.CHANNEL_ID)
        assertEquals(1001, UnreelForegroundService.NOTIFICATION_ID)
    }
}
