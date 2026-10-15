import Foundation
import DeviceActivity
import ManagedSettings

/**
 * DeviceActivityMonitor callback implementation for Unreel.
 * Receives threshold reach events and syncs usage to the App Group.
 */
class UnreelDeviceActivityMonitor: DeviceActivityMonitor {

    override func intervalDidStart(for activity: DeviceActivityName) {
        super.intervalDidStart(for: activity)
    }

    override func intervalDidEnd(for activity: DeviceActivityName) {
        super.intervalDidEnd(for: activity)
        // Reset shield at interval boundary
        ShieldManager.shared.clearShield()
    }

    override func eventDidReachThreshold(_ event: DeviceActivityEvent.Name, activity: DeviceActivityName) {
        super.eventDidReachThreshold(event, activity: activity)

        // Read snapshot from App Group
        let snapshot = SnapshotReader.readLatestSnapshot()

        // 1 minute tick = 60,000ms
        let tickDurationMs: Int64 = 60_000
        let updatedDurationMs = snapshot.todayDurationMs + tickDurationMs
        let updatedSessionMs = snapshot.activeSessionDurationMs + tickDurationMs

        // Check if limit progress exists and if blocked
        var isBlocked = snapshot.isBlocked
        if let progress = snapshot.limitProgress, progress >= 1.0 {
            isBlocked = true
        }

        let updatedSnapshot = WidgetUsageSnapshot(
            version: snapshot.version,
            date: snapshot.date,
            todayReels: snapshot.todayReels,
            todayDurationMs: updatedDurationMs,
            sessionReels: snapshot.sessionReels,
            activeSessionDurationMs: updatedSessionMs,
            limitProgress: snapshot.limitProgress,
            decisionReason: isBlocked ? "Daily Limit Reached" : snapshot.decisionReason,
            isBlocked: isBlocked,
            source: "ESTIMATED",
            updatedAtUtc: ISO8601DateFormatter().string(from: Date())
        )

        SnapshotReader.writeSnapshot(updatedSnapshot)
    }
}
