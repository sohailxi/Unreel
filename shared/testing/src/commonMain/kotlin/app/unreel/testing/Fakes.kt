package app.unreel.testing

import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.port.ScrollEventSource
import app.unreel.domain.port.UsageTickSource
import app.unreel.model.Decision
import app.unreel.model.ReelEvent
import app.unreel.model.UsageTick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Fake scroll event source for testing reel detection.
 */
class FakeScrollEventSource : ScrollEventSource {
    private val _events = MutableSharedFlow<ReelEvent>()
    override val events: Flow<ReelEvent> = _events

    suspend fun emit(event: ReelEvent) {
        _events.emit(event)
    }
}

/**
 * Fake usage tick source for testing iOS-style tick ingestion.
 */
class FakeUsageTickSource : UsageTickSource {
    private val _ticks = MutableSharedFlow<UsageTick>()
    override val ticks: Flow<UsageTick> = _ticks

    suspend fun emit(tick: UsageTick) {
        _ticks.emit(tick)
    }
}

/**
 * Fake enforcer that records all enforcement calls for assertion.
 */
class FakeEnforcer : Enforcer {
    val enforcedDecisions = mutableListOf<Decision>()
    var cleared = false
        private set

    override suspend fun enforce(decision: Decision) {
        enforcedDecisions.add(decision)
    }

    override suspend fun clearEnforcement() {
        cleared = true
    }
}

/**
 * Fake live surface publisher that records all published updates.
 */
class FakeLiveSurfacePublisher : LiveSurfacePublisher {
    data class Update(
        val sessionReels: Int,
        val todayReels: Int,
        val todayDurationMs: Long,
        val limitProgress: Float?,
    )

    val updates = mutableListOf<Update>()
    var sessionEnded = false
        private set

    override suspend fun publishSessionUpdate(
        sessionReels: Int,
        todayReels: Int,
        todayDurationMs: Long,
        limitProgress: Float?,
    ) {
        updates.add(Update(sessionReels, todayReels, todayDurationMs, limitProgress))
    }

    override suspend fun endSession() {
        sessionEnded = true
    }
}

/**
 * Fake notification port that records all notification calls.
 */
class FakeNotificationPort : NotificationPort {
    data class Notification(val title: String, val body: String)

    val nudges = mutableListOf<Notification>()
    val limitReached = mutableListOf<Notification>()
    val detectorBlind = mutableListOf<String>()
    val permissionRevoked = mutableListOf<String>()

    override suspend fun showNudge(title: String, body: String) {
        nudges.add(Notification(title, body))
    }

    override suspend fun showLimitReached(title: String, body: String) {
        limitReached.add(Notification(title, body))
    }

    override suspend fun showDetectorBlind(appName: String) {
        detectorBlind.add(appName)
    }

    override suspend fun showPermissionRevoked(permissionName: String) {
        permissionRevoked.add(permissionName)
    }
}
