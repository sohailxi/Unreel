import Foundation

/**
 * Swift model representing the lightweight versioned UsageSnapshot
 * published by the shared KMP core layer into the App Group.
 */
struct WidgetUsageSnapshot: Codable, Equatable {
    let version: Int
    let date: String
    let todayReels: Int
    let todayDurationMs: Int64
    let sessionReels: Int
    let activeSessionDurationMs: Int64
    let limitProgress: Float?
    let decisionReason: String?
    let isBlocked: Bool
    let source: String
    let updatedAtUtc: String

    var formattedDuration: String {
        let totalMinutes = max(0, todayDurationMs / 60_000)
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60
        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else {
            return "\(minutes)m"
        }
    }

    var formattedProgress: String {
        guard let p = limitProgress else { return "No limit" }
        return "\(Int(p * 100))%"
    }

    var statusText: String {
        if isBlocked {
            return "BLOCKED"
        } else if let p = limitProgress, p >= 0.85 {
            return "Nearing limit"
        } else {
            return "Within limits"
        }
    }

    static var placeholder: WidgetUsageSnapshot {
        WidgetUsageSnapshot(
            version: 1,
            date: "Today",
            todayReels: 28,
            todayDurationMs: 45 * 60 * 1000,
            sessionReels: 8,
            activeSessionDurationMs: 12 * 60 * 1000,
            limitProgress: 0.56,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: ISO8601DateFormatter().string(from: Date())
        )
    }
}

/**
 * Utility for reading and parsing the latest UsageSnapshot from the shared App Group.
 */
enum SnapshotReader {
    static let appGroupId = "group.app.unreel.shared"
    static let snapshotKey = "usage_snapshot_v1"

    static func readLatestSnapshot() -> WidgetUsageSnapshot {
        guard let userDefaults = UserDefaults(suiteName: appGroupId) else {
            return .placeholder
        }

        guard let jsonString = userDefaults.string(forKey: snapshotKey),
              let data = jsonString.data(using: .utf8) else {
            return .placeholder
        }

        do {
            let decoder = JSONDecoder()
            return try decoder.decode(WidgetUsageSnapshot.self, from: data)
        } catch {
            return .placeholder
        }
    }

    static func writeSnapshot(_ snapshot: WidgetUsageSnapshot) {
        guard let userDefaults = UserDefaults(suiteName: appGroupId) else { return }
        do {
            let encoder = JSONEncoder()
            let data = try encoder.encode(snapshot)
            if let string = String(data: data, encoding: .utf8) {
                userDefaults.set(string, forKey: snapshotKey)
            }
        } catch {
            // Log or ignore
        }
    }
}
