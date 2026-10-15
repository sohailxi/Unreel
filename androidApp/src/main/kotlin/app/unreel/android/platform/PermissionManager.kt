package app.unreel.android.platform

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import app.unreel.android.service.ReelDetectorService
import app.unreel.android.service.UnreelForegroundService
import co.touchlab.kermit.Logger

/**
 * State representing granted permissions and foreground service status on Android.
 */
data class PermissionStatus(
    val isAccessibilityEnabled: Boolean = false,
    val isOverlayGranted: Boolean = false,
    val isNotificationGranted: Boolean = false,
    val isBatteryOptimizationIgnored: Boolean = false,
    val isServiceRunning: Boolean = false,
) {
    val areCorePermissionsGranted: Boolean
        get() = isAccessibilityEnabled && isOverlayGranted && isNotificationGranted

    val isFullyConfigured: Boolean
        get() = areCorePermissionsGranted && isBatteryOptimizationIgnored && isServiceRunning
}

/**
 * Interface defining permission checks, settings intent dispatch, and service controls.
 */
interface PermissionManager {
    fun getPermissionStatus(): PermissionStatus
    fun isAccessibilityEnabled(): Boolean
    fun isOverlayGranted(): Boolean
    fun isNotificationGranted(): Boolean
    fun isBatteryOptimizationIgnored(): Boolean
    fun isServiceRunning(): Boolean

    fun openAccessibilitySettings()
    fun openOverlaySettings()
    fun openNotificationSettings()
    fun requestIgnoreBatteryOptimizations()
    fun startMonitoringService()
    fun stopMonitoringService()
}

/**
 * Android implementation of [PermissionManager].
 */
class AndroidPermissionManager(
    private val context: Context?,
    private val logger: Logger = Logger.withTag("AndroidPermissionManager"),
) : PermissionManager {

    override fun isAccessibilityEnabled(): Boolean {
        val ctx = context ?: return false
        if (ReelDetectorService.isConnected.value) {
            return true
        }
        return isAccessibilityServiceRunning(ctx, ReelDetectorService::class.java)
    }

    override fun isOverlayGranted(): Boolean {
        val ctx = context ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(ctx)
        } else {
            true
        }
    }

    override fun isNotificationGranted(): Boolean {
        val ctx = context ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        }
    }

    override fun isBatteryOptimizationIgnored(): Boolean {
        val ctx = context ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = ctx.getSystemService(Context.POWER_SERVICE) as? PowerManager
            powerManager?.isIgnoringBatteryOptimizations(ctx.packageName) == true
        } else {
            true
        }
    }

    override fun isServiceRunning(): Boolean {
        return UnreelForegroundService.isRunning.value
    }

    override fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            isAccessibilityEnabled = isAccessibilityEnabled(),
            isOverlayGranted = isOverlayGranted(),
            isNotificationGranted = isNotificationGranted(),
            isBatteryOptimizationIgnored = isBatteryOptimizationIgnored(),
            isServiceRunning = isServiceRunning(),
        )
    }

    override fun openAccessibilitySettings() {
        val ctx = context ?: return
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(intent)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to launch accessibility settings" }
        }
    }

    override fun openOverlaySettings() {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${ctx.packageName}"),
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(intent)
            } catch (_: Throwable) {
                // Fallback to general overlay settings
                val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                runCatching { ctx.startActivity(fallbackIntent) }
            }
        }
    }

    override fun openNotificationSettings() {
        val ctx = context ?: return
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            ctx.startActivity(intent)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to launch notification settings" }
        }
    }

    override fun requestIgnoreBatteryOptimizations() {
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${ctx.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                ctx.startActivity(intent)
            } catch (_: Throwable) {
                val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                runCatching { ctx.startActivity(fallbackIntent) }
            }
        }
    }

    override fun startMonitoringService() {
        val ctx = context ?: return
        try {
            UnreelForegroundService.start(ctx)
            logger.i { "Requested UnreelForegroundService start" }
        } catch (t: Throwable) {
            logger.e(t) { "Failed to start UnreelForegroundService" }
        }
    }

    override fun stopMonitoringService() {
        val ctx = context ?: return
        try {
            UnreelForegroundService.stop(ctx)
            logger.i { "Requested UnreelForegroundService stop" }
        } catch (t: Throwable) {
            logger.e(t) { "Failed to stop UnreelForegroundService" }
        }
    }

    companion object {
        fun isAccessibilityServiceRunning(
            context: Context,
            serviceClass: Class<out AccessibilityService>,
        ): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val enabledServices = am?.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            if (enabledServices?.any {
                it.resolveInfo.serviceInfo.packageName == context.packageName &&
                    it.resolveInfo.serviceInfo.name == serviceClass.name
            } == true) {
                return true
            }

            return try {
                val enabledServicesSetting = Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                ) ?: return false

                val expected1 = "${context.packageName}/${serviceClass.name}"
                val expected2 = "${context.packageName}/${serviceClass.canonicalName}"
                val splitter = TextUtils.SimpleStringSplitter(':')
                splitter.setString(enabledServicesSetting)
                while (splitter.hasNext()) {
                    val component = splitter.next()
                    if (component.equals(expected1, ignoreCase = true) ||
                        component.equals(expected2, ignoreCase = true)
                    ) {
                        return true
                    }
                }
                false
            } catch (_: Throwable) {
                false
            }
        }
    }
}
