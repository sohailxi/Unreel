package app.unreel.platform

import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.model.Decision
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSBundle
import platform.Foundation.NSUUID
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter

/**
 * Live surface metrics for iOS widgets and Live Activities.
 */
data class IosSurfaceState(
    val sessionReels: Int = 0,
    val todayReels: Int = 0,
    val todayDurationMs: Long = 0L,
    val limitProgress: Float? = null,
    val isSessionActive: Boolean = false,
)

/**
 * iOS implementation of [LiveSurfacePublisher].
 * Updates surface state flow and triggers native widget / Live Activity listeners.
 */
class IosLiveSurfacePublisher(
    private val logger: Logger = Logger.withTag("IosLiveSurfacePublisher"),
    private val onUpdateCallback: ((IosSurfaceState) -> Unit)? = null,
) : LiveSurfacePublisher {
    private val _surfaceState = MutableStateFlow(IosSurfaceState())
    val surfaceState: StateFlow<IosSurfaceState> = _surfaceState.asStateFlow()

    override suspend fun publishSessionUpdate(
        sessionReels: Int,
        todayReels: Int,
        todayDurationMs: Long,
        limitProgress: Float?,
    ) {
        val newState = IosSurfaceState(
            sessionReels = sessionReels,
            todayReels = todayReels,
            todayDurationMs = todayDurationMs,
            limitProgress = limitProgress,
            isSessionActive = true,
        )
        _surfaceState.value = newState
        logger.d { "Published iOS surface update: todayReels=$todayReels, sessionReels=$sessionReels" }
        onUpdateCallback?.invoke(newState)
    }

    override suspend fun endSession() {
        val newState = _surfaceState.value.copy(
            isSessionActive = false,
            sessionReels = 0,
        )
        _surfaceState.value = newState
        logger.d { "Ended iOS live surface session" }
        onUpdateCallback?.invoke(newState)
    }
}

/**
 * iOS implementation of [Enforcer].
 * Holds current decision state and notifies native shield / managed settings delegates.
 */
class IosEnforcer(
    private val logger: Logger = Logger.withTag("IosEnforcer"),
    private val onDecisionChanged: ((Decision?) -> Unit)? = null,
) : Enforcer {
    private val _currentDecision = MutableStateFlow<Decision?>(null)
    val currentDecision: StateFlow<Decision?> = _currentDecision.asStateFlow()

    override suspend fun enforce(decision: Decision) {
        _currentDecision.value = decision
        logger.i { "Enforcing decision on iOS: $decision" }
        onDecisionChanged?.invoke(decision)
    }

    override suspend fun clearEnforcement() {
        _currentDecision.value = null
        logger.i { "Cleared iOS enforcement" }
        onDecisionChanged?.invoke(null)
    }
}

/**
 * iOS implementation of [NotificationPort] using [UNUserNotificationCenter].
 */
class IosNotificationPort(
    private val logger: Logger = Logger.withTag("IosNotificationPort"),
    private val sender: ((title: String, body: String) -> Unit)? = null,
) : NotificationPort {

    override suspend fun showNudge(title: String, body: String) {
        dispatchLocalNotification(title, body)
    }

    override suspend fun showLimitReached(title: String, body: String) {
        dispatchLocalNotification(title, body)
    }

    override suspend fun showDetectorBlind(appName: String) {
        dispatchLocalNotification("Unreel Alert", "Tracking is unavailable for $appName")
    }

    override suspend fun showPermissionRevoked(permissionName: String) {
        dispatchLocalNotification("Permission Required", "$permissionName permission was revoked.")
    }

    private fun dispatchLocalNotification(title: String, body: String) {
        if (sender != null) {
            sender.invoke(title, body)
            return
        }

        // If running in a test host or CLI environment without a valid bundle identifier, do not call UNUserNotificationCenter
        if (NSBundle.mainBundle.bundleIdentifier == null) {
            logger.d { "Skipping notification in non-bundled environment: $title - $body" }
            return
        }

        try {
            val content = UNMutableNotificationContent().apply {
                setTitle(title)
                setBody(body)
            }
            val request = UNNotificationRequest.requestWithIdentifier(
                identifier = NSUUID().UUIDString(),
                content = content,
                trigger = null,
            )
            UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
                if (error != null) {
                    logger.w { "Failed to schedule notification: ${error.localizedDescription}" }
                }
            }
        } catch (t: Throwable) {
            logger.w(t) { "Unable to schedule iOS local notification" }
        }
    }
}
