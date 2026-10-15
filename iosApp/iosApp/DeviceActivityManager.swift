import Foundation
import DeviceActivity
import FamilyControls

/**
 * Coordinates DeviceActivity schedules and threshold events
 * for estimated short-video usage tracking on iOS.
 */
final class DeviceActivityManager {
    static let shared = DeviceActivityManager()

    private let center = DeviceActivityCenter()
    static let activityName = DeviceActivityName("app.unreel.ios.activity.monitor")
    static let eventName = DeviceActivityEvent.Name("app.unreel.ios.event.tick")

    /**
     * Starts monitoring for the given selection with 1-minute usage thresholds.
     */
    func startMonitoring(for selection: FamilyActivitySelection) {
        guard !selection.applicationTokens.isEmpty || !selection.categoryTokens.isEmpty else {
            stopMonitoring()
            return
        }

        // 24-hour daily schedule starting at 04:00 AM (default day start hour)
        let schedule = DeviceActivitySchedule(
            intervalStart: DateComponents(hour: 4, minute: 0),
            intervalEnd: DateComponents(hour: 3, minute: 59),
            repeats: true
        )

        // Monitor at 1-minute increments for usage ticks
        let event = DeviceActivityEvent(
            applications: selection.applicationTokens,
            categories: selection.categoryTokens,
            webDomains: selection.webDomainTokens,
            threshold: DateComponents(minute: 1)
        )

        do {
            try center.startMonitoring(
                Self.activityName,
                during: schedule,
                events: [Self.eventName: event]
            )
        } catch {
            // Failed to start monitoring or simulator limitation
        }
    }

    func stopMonitoring() {
        center.stopMonitoring([Self.activityName])
    }
}
