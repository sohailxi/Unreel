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
    val isAggressiveOem: Boolean = false,
    val deviceManufacturer: String = "",
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
    fun isAggressiveOem(): Boolean
    fun getDeviceManufacturer(): String

    fun openAccessibilitySettings()
    fun openOverlaySettings()
    fun openNotificationSettings()
    fun requestIgnoreBatteryOptimizations()
    fun openAutostartSettings()
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

    override fun isAggressiveOem(): Boolean {
        return isManufacturerAggressive(Build.MANUFACTURER)
    }

    override fun getDeviceManufacturer(): String {
        return formatManufacturer(Build.MANUFACTURER)
    }

    override fun getPermissionStatus(): PermissionStatus {
        return PermissionStatus(
            isAccessibilityEnabled = isAccessibilityEnabled(),
            isOverlayGranted = isOverlayGranted(),
            isNotificationGranted = isNotificationGranted(),
            isBatteryOptimizationIgnored = isBatteryOptimizationIgnored(),
            isServiceRunning = isServiceRunning(),
            isAggressiveOem = isAggressiveOem(),
            deviceManufacturer = getDeviceManufacturer(),
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

    override fun openAutostartSettings() {
        val ctx = context ?: return
        val manufacturer = Build.MANUFACTURER.orEmpty()
        val intents = getOemAutostartIntents(ctx, manufacturer)
        for (intent in intents) {
            try {
                ctx.startActivity(intent)
                logger.i { "Launched autostart intent for $manufacturer" }
                return
            } catch (_: Throwable) {
                // Try next candidate
            }
        }
        try {
            val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${ctx.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            ctx.startActivity(fallbackIntent)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to launch fallback application settings" }
        }
    }

    companion object {
        fun formatManufacturer(manufacturer: String?): String {
            val raw = manufacturer?.trim().orEmpty()
            return if (raw.isNotEmpty()) {
                raw.replaceFirstChar { it.uppercase() }
            } else {
                "Android"
            }
        }

        fun isManufacturerAggressive(manufacturer: String?): Boolean {
            val m = manufacturer?.lowercase().orEmpty()
            return m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") ||
                m.contains("oppo") || m.contains("realme") || m.contains("oneplus") ||
                m.contains("vivo") || m.contains("iqoo") ||
                m.contains("huawei") || m.contains("honor") ||
                m.contains("samsung") || m.contains("asus")
        }

        fun getOemAutostartIntents(context: Context, manufacturer: String): List<Intent> {
            val m = manufacturer.lowercase()
            val intents = mutableListOf<Intent>()

            when {
                m.contains("xiaomi") || m.contains("redmi") || m.contains("poco") -> {
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                }
                m.contains("oppo") || m.contains("realme") || m.contains("oneplus") -> {
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.coloros.safecenter",
                                "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.oppo.safe",
                                "com.oppo.safe.permission.startup.StartupAppListActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                }
                m.contains("vivo") || m.contains("iqoo") -> {
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.iqoo.secure",
                                "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.vivo.permissionmanager",
                                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                }
                m.contains("huawei") || m.contains("honor") -> {
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.huawei.systemmanager",
                                "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                }
                m.contains("samsung") -> {
                    intents.add(
                        Intent().apply {
                            component = ComponentName(
                                "com.samsung.android.lool",
                                "com.samsung.android.sm.ui.battery.BatteryActivity",
                            )
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        },
                    )
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intents.add(
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    },
                )
            }
            intents.add(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
            )

            return intents
        }

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
