import ActivityKit
import Foundation

/**
 * ActivityKit attributes for live scroll sessions.
 * Shared between main app (to start/update activities) and widget extension (to render UI).
 */
struct UnreelActivityAttributes: ActivityAttributes {
    public struct ContentState: Codable, Hashable {
        var sessionReels: Int
        var todayReels: Int
        var sessionDurationMinutes: Int
        var limitProgress: Float?
        var isNearingLimit: Bool
        var isBlocked: Bool
        var source: String // Binding rule: must be "ESTIMATED" on iOS

        static var sample: ContentState {
            ContentState(
                sessionReels: 14,
                todayReels: 38,
                sessionDurationMinutes: 18,
                limitProgress: 0.65,
                isNearingLimit: false,
                isBlocked: false,
                source: "ESTIMATED"
            )
        }
    }

    var appName: String
}
