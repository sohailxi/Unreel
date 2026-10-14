package app.unreel.domain.port

/**
 * Port for publishing live surface updates (popup counter, Live Activity, widgets).
 * Android: floating overlay + Glance widget update.
 * iOS: ActivityKit Live Activity update + WidgetKit timeline reload.
 */
interface LiveSurfacePublisher {
    suspend fun publishSessionUpdate(
        sessionReels: Int,
        todayReels: Int,
        todayDurationMs: Long,
        limitProgress: Float?, // 0.0..1.0, null if no limit set
    )

    suspend fun endSession()
}

/**
 * Port for sending notifications.
 */
interface NotificationPort {
    suspend fun showNudge(title: String, body: String)
    suspend fun showLimitReached(title: String, body: String)
    suspend fun showDetectorBlind(appName: String)
    suspend fun showPermissionRevoked(permissionName: String)
}
