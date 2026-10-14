package app.unreel.android.platform

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import app.unreel.domain.port.NotificationPort
import co.touchlab.kermit.Logger

/**
 * Android implementation of [NotificationPort].
 * Dispatches system notifications for limits, nudges, and permission alerts.
 */
class AndroidNotificationPort(
    private val context: Context? = null,
    private val logger: Logger = Logger.withTag("AndroidNotificationPort"),
) : NotificationPort {

    companion object {
        const val CHANNEL_LIMITS = "unreel_limits"
        const val CHANNEL_NUDGES = "unreel_nudges"
        const val CHANNEL_SYSTEM = "unreel_system"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && context != null) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val channels = listOf(
                NotificationChannel(CHANNEL_LIMITS, "Limits & Enforcement", NotificationManager.IMPORTANCE_HIGH),
                NotificationChannel(CHANNEL_NUDGES, "Usage Nudges", NotificationManager.IMPORTANCE_DEFAULT),
                NotificationChannel(CHANNEL_SYSTEM, "System Alerts", NotificationManager.IMPORTANCE_HIGH),
            )
            channels.forEach { notificationManager?.createNotificationChannel(it) }
        }
    }

    override suspend fun showNudge(title: String, body: String) {
        logger.i { "Nudge notification: $title - $body" }
        postNotification(CHANNEL_NUDGES, 1001, title, body)
    }

    override suspend fun showLimitReached(title: String, body: String) {
        logger.w { "Limit reached notification: $title - $body" }
        postNotification(CHANNEL_LIMITS, 1002, title, body)
    }

    override suspend fun showDetectorBlind(appName: String) {
        val title = "Tracking Paused"
        val body = "Accessibility detection paused for $appName. Tap to re-enable."
        logger.w { "Detector blind: $title - $body" }
        postNotification(CHANNEL_SYSTEM, 1003, title, body)
    }

    override suspend fun showPermissionRevoked(permissionName: String) {
        val title = "Permission Required"
        val body = "Unreel requires $permissionName to protect your screen time."
        logger.e { "Permission revoked: $title - $body" }
        postNotification(CHANNEL_SYSTEM, 1004, title, body)
    }

    private fun postNotification(channelId: String, notificationId: Int, title: String, body: String) {
        val ctx = context ?: return
        try {
            val notification = NotificationCompat.Builder(ctx, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            val notificationManager =
                ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.notify(notificationId, notification)
        } catch (t: Throwable) {
            logger.e(t) { "Failed to post notification: $title" }
        }
    }
}
