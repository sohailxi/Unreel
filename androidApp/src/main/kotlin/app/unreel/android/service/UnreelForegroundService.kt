package app.unreel.android.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.unreel.android.MainActivity
import app.unreel.android.R
import app.unreel.android.platform.AndroidLiveSurfacePublisher
import app.unreel.android.platform.AndroidOverlayEnforcer
import app.unreel.android.platform.SurfaceState
import app.unreel.model.Decision
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Ongoing foreground service providing continuous presence and survival across
 * OEM battery optimizations (ADR-005, Spike S5).
 *
 * Displays ongoing reel scroll counters and today's dwell time in real-time.
 */
class UnreelForegroundService : Service(), KoinComponent {

    private val logger = Logger.withTag("UnreelForegroundService")
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val liveSurfacePublisher: AndroidLiveSurfacePublisher by inject()
    private val overlayEnforcer: AndroidOverlayEnforcer by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true
        logger.i { "UnreelForegroundService created" }
        createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            logger.i { "Stop action received, stopping foreground service" }
            _isRunning.value = false
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        _isRunning.value = true
        logger.i { "Starting UnreelForegroundService ongoing notification" }
        val initialNotification = buildNotification(
            context = this,
            surfaceState = liveSurfacePublisher.surfaceState.value,
            decision = overlayEnforcer.activeDecision.value,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }

        observeMetricsAndDecisions()
        return START_STICKY
    }

    private fun observeMetricsAndDecisions() {
        serviceScope.launch {
            combine(
                liveSurfacePublisher.surfaceState,
                overlayEnforcer.activeDecision,
            ) { surfaceState, decision ->
                surfaceState to decision
            }.collect { (surfaceState, decision) ->
                val notification = buildNotification(
                    context = this@UnreelForegroundService,
                    surfaceState = surfaceState,
                    decision = decision,
                )
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _isRunning.value = false
        logger.i { "UnreelForegroundService destroyed" }
        serviceScope.cancel()
    }

    companion object {
        private val _isRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isRunning: kotlinx.coroutines.flow.StateFlow<Boolean> = _isRunning

        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "unreel_monitor_channel"
        const val ACTION_START = "app.unreel.android.action.START_MONITORING"
        const val ACTION_STOP = "app.unreel.android.action.STOP_MONITORING"

        fun start(context: Context) {
            val intent = Intent(context, UnreelForegroundService::class.java).apply {
                action = ACTION_START
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, UnreelForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Scroll Monitor",
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = "Persistent indicator displaying real-time reel counts and active scroll time"
                    setShowBadge(false)
                }
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                notificationManager?.createNotificationChannel(channel)
            }
        }

        fun formatDuration(durationMs: Long): String {
            val totalMinutes = durationMs / 60_000L
            val hours = totalMinutes / 60L
            val minutes = totalMinutes % 60L
            return when {
                hours > 0L && minutes > 0L -> "${hours}h ${minutes}m"
                hours > 0L -> "${hours}h"
                else -> "${minutes}m"
            }
        }

        fun getNotificationTitle(decision: Decision, limitProgress: Float?): String {
            return when {
                decision is Decision.Block -> "⚠️ Reel Limit Reached"
                decision is Decision.Friction -> "⏳ Pause & Breathe"
                limitProgress != null && limitProgress >= 0.85f -> "⚡ Nearing Daily Limit"
                else -> "Unreel Active"
            }
        }

        fun getNotificationContent(surfaceState: SurfaceState, decision: Decision): String {
            return when (decision) {
                is Decision.Block -> decision.reason
                is Decision.Friction -> decision.reason
                else -> {
                    val durationStr = formatDuration(surfaceState.todayDurationMs)
                    val base = "${surfaceState.todayReels} reels today ($durationStr)"
                    if (surfaceState.isSessionActive) {
                        "$base • ${surfaceState.sessionReels} in session"
                    } else {
                        base
                    }
                }
            }
        }

        fun buildNotification(
            context: Context,
            surfaceState: SurfaceState,
            decision: Decision,
        ): Notification {
            val title = getNotificationTitle(decision, surfaceState.limitProgress)
            val content = getNotificationContent(surfaceState, decision)

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            val openPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                pendingIntentFlags,
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(openPendingIntent)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)

            surfaceState.limitProgress?.let { progress ->
                val progressInt = (progress * 100).toInt().coerceIn(0, 100)
                builder.setProgress(100, progressInt, false)
            }

            return builder.build()
        }
    }
}
