package app.unreel.android.platform

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidNotificationPortTest {

    @Test
    fun `showNudge completes without throwing when context is null`() = runTest {
        val notificationPort = AndroidNotificationPort(context = null)
        notificationPort.showNudge(title = "Nudge Title", body = "Take a break")
        assertNotNull(notificationPort)
    }

    @Test
    fun `showLimitReached completes without throwing when context is null`() = runTest {
        val notificationPort = AndroidNotificationPort(context = null)
        notificationPort.showLimitReached(title = "Limit Reached", body = "Daily limit reached")
        assertNotNull(notificationPort)
    }

    @Test
    fun `showDetectorBlind completes without throwing when context is null`() = runTest {
        val notificationPort = AndroidNotificationPort(context = null)
        notificationPort.showDetectorBlind(appName = "Instagram")
        assertNotNull(notificationPort)
    }

    @Test
    fun `showPermissionRevoked completes without throwing when context is null`() = runTest {
        val notificationPort = AndroidNotificationPort(context = null)
        notificationPort.showPermissionRevoked(permissionName = "Accessibility")
        assertNotNull(notificationPort)
    }
}
