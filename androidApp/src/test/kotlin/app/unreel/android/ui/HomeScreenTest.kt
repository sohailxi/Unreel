package app.unreel.android.ui

import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.model.FocusScore
import app.unreel.model.Session
import app.unreel.presentation.home.HomeState
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeScreenTest {

    @Test
    fun `formatDwellTime formats milliseconds into readable minutes and seconds`() {
        assertEquals("0m", formatDwellTime(0L))
        assertEquals("1m 30s", formatDwellTime(90_000L))
        assertEquals("15m", formatDwellTime(900_000L))
        assertEquals("1h 15m", formatDwellTime(4_500_000L))
    }

    @Test
    fun `calculateGoalProgress calculates clamped progress fraction`() {
        assertEquals(0f, calculateGoalProgress(currentReels = 0, targetReels = 50))
        assertEquals(0.5f, calculateGoalProgress(currentReels = 25, targetReels = 50))
        assertEquals(1.0f, calculateGoalProgress(currentReels = 50, targetReels = 50))
        assertEquals(1.0f, calculateGoalProgress(currentReels = 75, targetReels = 50))
        assertEquals(0f, calculateGoalProgress(currentReels = 10, targetReels = 0))
    }

    @Test
    fun `homeState decision detection`() {
        val allowState = HomeState(currentDecision = Decision.Allow)
        assertFalse(allowState.isRestricted)

        val blockState = HomeState(
            currentDecision = Decision.Block(
                until = null,
                reason = "Daily limit",
                ruleId = "r1",
                canBypass = true,
                passesRemaining = 1,
            )
        )
        assertTrue(blockState.isRestricted)
    }

    @Test
    fun `getServiceStatusHeader returns accurate titles and descriptions for PermissionStatus`() {
        val fullyConfigured = app.unreel.android.platform.PermissionStatus(
            isAccessibilityEnabled = true,
            isOverlayGranted = true,
            isNotificationGranted = true,
            isBatteryOptimizationIgnored = true,
            isServiceRunning = true,
        )
        val (fullTitle, fullDesc) = getServiceStatusHeader(fullyConfigured)
        assertEquals("Active Protection", fullTitle)
        assertTrue(fullDesc.contains("active"))

        val serviceInactive = fullyConfigured.copy(isServiceRunning = false)
        val (inactiveTitle, inactiveDesc) = getServiceStatusHeader(serviceInactive)
        assertEquals("Service Inactive", inactiveTitle)
        assertTrue(inactiveDesc.contains("Start"))

        val permissionsRequired = fullyConfigured.copy(isAccessibilityEnabled = false)
        val (permTitle, permDesc) = getServiceStatusHeader(permissionsRequired)
        assertEquals("Permissions Required", permTitle)
        assertTrue(permDesc.contains("Grant"))

        val setupRequired = app.unreel.android.platform.PermissionStatus()
        val (setupTitle, setupDesc) = getServiceStatusHeader(setupRequired)
        assertEquals("Setup Required", setupTitle)
        assertTrue(setupDesc.contains("permissions"))
    }

    @Test
    fun `getTrackedAppLabel returns friendly label for specific apps and default for all`() {
        assertEquals("All Feeds & Reels", getTrackedAppLabel(null))
        assertEquals("Instagram Reels", getTrackedAppLabel("com.instagram.android"))
        assertEquals("YouTube Shorts", getTrackedAppLabel("com.google.android.youtube"))
        assertEquals("Moj", getTrackedAppLabel("in.mohalla.video"))
    }

    @Test
    fun `rankAppUsage sorts by reels descending and filters empty records`() {
        val today = LocalDate(2026, 10, 16)
        val usage1 = DailyUsage(today, "com.instagram.android", 10, 300_000L, 1, 1, DataSource.EXACT)
        val usage2 = DailyUsage(today, "com.google.android.youtube", 45, 1_200_000L, 2, 2, DataSource.EXACT)
        val usage3 = DailyUsage(today, "com.zhiliaoapp.musically", 0, 0L, 0, 0, DataSource.EXACT)
        val usageAggregate = DailyUsage(today, "aggregate", 55, 1_500_000L, 3, 3, DataSource.EXACT)

        val ranked = rankAppUsage(listOf(usage1, usage2, usage3, usageAggregate))
        assertEquals(2, ranked.size)
        assertEquals("com.google.android.youtube", ranked[0].appId)
        assertEquals(45, ranked[0].reels)
        assertEquals("com.instagram.android", ranked[1].appId)
        assertEquals(10, ranked[1].reels)
    }

    @Test
    fun `calculateReelPercentage and calculateBarFraction compute correct values`() {
        assertEquals(50, calculateReelPercentage(reels = 25, totalReels = 50))
        assertEquals(100, calculateReelPercentage(reels = 50, totalReels = 50))
        assertEquals(0, calculateReelPercentage(reels = 0, totalReels = 50))
        assertEquals(0, calculateReelPercentage(reels = 10, totalReels = 0))

        assertEquals(0.5f, calculateBarFraction(reels = 25, maxReels = 50))
        assertEquals(1f, calculateBarFraction(reels = 50, maxReels = 50))
        assertEquals(0f, calculateBarFraction(reels = 0, maxReels = 50))
    }
}


