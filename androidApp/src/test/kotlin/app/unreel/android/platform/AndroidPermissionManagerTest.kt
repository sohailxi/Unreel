package app.unreel.android.platform

import app.unreel.android.service.ReelDetectorService
import app.unreel.android.service.UnreelForegroundService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidPermissionManagerTest {

    @Test
    fun `PermissionStatus areCorePermissionsGranted requires accessibility, overlay, and notification`() {
        val allGranted = PermissionStatus(
            isAccessibilityEnabled = true,
            isOverlayGranted = true,
            isNotificationGranted = true,
            isBatteryOptimizationIgnored = false,
            isServiceRunning = false,
        )
        assertTrue(allGranted.areCorePermissionsGranted)
        assertFalse(allGranted.isFullyConfigured)

        val missingAccessibility = allGranted.copy(isAccessibilityEnabled = false)
        assertFalse(missingAccessibility.areCorePermissionsGranted)

        val missingOverlay = allGranted.copy(isOverlayGranted = false)
        assertFalse(missingOverlay.areCorePermissionsGranted)

        val missingNotification = allGranted.copy(isNotificationGranted = false)
        assertFalse(missingNotification.areCorePermissionsGranted)
    }

    @Test
    fun `PermissionStatus isFullyConfigured requires core permissions, battery exemption, and active service`() {
        val fullyConfigured = PermissionStatus(
            isAccessibilityEnabled = true,
            isOverlayGranted = true,
            isNotificationGranted = true,
            isBatteryOptimizationIgnored = true,
            isServiceRunning = true,
        )
        assertTrue(fullyConfigured.isFullyConfigured)
        assertTrue(fullyConfigured.areCorePermissionsGranted)

        val stoppedService = fullyConfigured.copy(isServiceRunning = false)
        assertFalse(stoppedService.isFullyConfigured)
        assertTrue(stoppedService.areCorePermissionsGranted)

        val notExempt = fullyConfigured.copy(isBatteryOptimizationIgnored = false)
        assertFalse(notExempt.isFullyConfigured)
    }

    @Test
    fun `AndroidPermissionManager handles null context safely without throwing`() {
        val manager = AndroidPermissionManager(context = null)

        assertFalse(manager.isAccessibilityEnabled())
        assertFalse(manager.isOverlayGranted())
        assertFalse(manager.isNotificationGranted())
        assertFalse(manager.isBatteryOptimizationIgnored())
        assertFalse(manager.isServiceRunning())

        val status = manager.getPermissionStatus()
        assertFalse(status.areCorePermissionsGranted)
        assertFalse(status.isFullyConfigured)

        // Actions should safely no-op when context is null
        manager.openAccessibilitySettings()
        manager.openOverlaySettings()
        manager.openNotificationSettings()
        manager.requestIgnoreBatteryOptimizations()
        manager.startMonitoringService()
        manager.stopMonitoringService()
    }

    @Test
    fun `UnreelForegroundService and ReelDetectorService expose reactive status flows`() {
        assertFalse(UnreelForegroundService.isRunning.value)
        assertFalse(ReelDetectorService.isConnected.value)
    }

    @Test
    fun `isManufacturerAggressive correctly identifies aggressive OEM brands`() {
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("Xiaomi"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("redmi"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("Poco"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("Samsung"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("OPPO"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("Vivo"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("OnePlus"))
        assertTrue(AndroidPermissionManager.isManufacturerAggressive("Huawei"))
        assertFalse(AndroidPermissionManager.isManufacturerAggressive("Google"))
        assertFalse(AndroidPermissionManager.isManufacturerAggressive("Motorola"))
    }

    @Test
    fun `openAutostartSettings handles null context without throwing`() {
        val manager = AndroidPermissionManager(context = null)
        manager.openAutostartSettings()
        kotlin.test.assertNotNull(manager.getDeviceManufacturer())
    }
}

