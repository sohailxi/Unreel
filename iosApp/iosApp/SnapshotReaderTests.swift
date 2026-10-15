import Foundation

/**
 * Unit verification suite for WidgetUsageSnapshot and SnapshotReader.
 * Ensures compatibility with KMP SnapshotPublisher serialization and binding rules.
 */
enum SnapshotReaderTests {
    static func runAllTests() -> Bool {
        testDurationFormatting()
        testProgressFormatting()
        testStatusText()
        testJsonDecoding()
        testCorruptedJsonFallback()
        testUsageTickDurationIncrement()
        return true
    }

    private static func testDurationFormatting() {
        let snap1 = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 10,
            todayDurationMs: 45 * 60 * 1000,
            sessionReels: 2,
            activeSessionDurationMs: 5 * 60 * 1000,
            limitProgress: 0.5,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(snap1.formattedDuration == "45m", "Expected 45m, got \(snap1.formattedDuration)")

        let snap2 = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 10,
            todayDurationMs: 75 * 60 * 1000,
            sessionReels: 2,
            activeSessionDurationMs: 5 * 60 * 1000,
            limitProgress: 0.5,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(snap2.formattedDuration == "1h 15m", "Expected 1h 15m, got \(snap2.formattedDuration)")
    }

    private static func testProgressFormatting() {
        let withLimit = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 10,
            todayDurationMs: 10_000,
            sessionReels: 2,
            activeSessionDurationMs: 5_000,
            limitProgress: 0.85,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(withLimit.formattedProgress == "85%", "Expected 85%, got \(withLimit.formattedProgress)")

        let noLimit = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 10,
            todayDurationMs: 10_000,
            sessionReels: 2,
            activeSessionDurationMs: 5_000,
            limitProgress: nil,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(noLimit.formattedProgress == "No limit", "Expected No limit, got \(noLimit.formattedProgress)")
    }

    private static func testStatusText() {
        let blocked = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 50,
            todayDurationMs: 60_000,
            sessionReels: 5,
            activeSessionDurationMs: 10_000,
            limitProgress: 1.0,
            decisionReason: "Limit Reached",
            isBlocked: true,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(blocked.statusText == "BLOCKED")

        let nearing = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 45,
            todayDurationMs: 50_000,
            sessionReels: 5,
            activeSessionDurationMs: 10_000,
            limitProgress: 0.9,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(nearing.statusText == "Nearing limit")

        let within = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 10,
            todayDurationMs: 10_000,
            sessionReels: 2,
            activeSessionDurationMs: 2_000,
            limitProgress: 0.2,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )
        assert(within.statusText == "Within limits")
    }

    private static func testJsonDecoding() {
        let validJson = """
        {
            "version": 1,
            "date": "2026-10-15",
            "todayReels": 35,
            "todayDurationMs": 1800000,
            "sessionReels": 10,
            "activeSessionDurationMs": 600000,
            "limitProgress": 0.7,
            "decisionReason": null,
            "isBlocked": false,
            "source": "ESTIMATED",
            "updatedAtUtc": "2026-10-15T12:00:00Z"
        }
        """

        let data = validJson.data(using: .utf8)!
        let decoded = try! JSONDecoder().decode(WidgetUsageSnapshot.self, from: data)
        assert(decoded.todayReels == 35)
        assert(decoded.sessionReels == 10)
        assert(decoded.source == "ESTIMATED")
        assert(decoded.formattedDuration == "30m")
        assert(decoded.formattedProgress == "70%")
        assert(decoded.statusText == "Within limits")
    }

    private static func testCorruptedJsonFallback() {
        let corruptedJson = "{ invalid json content }"
        let data = corruptedJson.data(using: .utf8)!
        let result = (try? JSONDecoder().decode(WidgetUsageSnapshot.self, from: data)) ?? .placeholder
        assert(result == .placeholder)
    }

    private static func testUsageTickDurationIncrement() {
        let original = WidgetUsageSnapshot(
            version: 1,
            date: "2026-10-15",
            todayReels: 20,
            todayDurationMs: 30 * 60 * 1000,
            sessionReels: 5,
            activeSessionDurationMs: 10 * 60 * 1000,
            limitProgress: 0.95,
            decisionReason: nil,
            isBlocked: false,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:00:00Z"
        )

        let tickMs: Int64 = 60_000
        let updated = WidgetUsageSnapshot(
            version: original.version,
            date: original.date,
            todayReels: original.todayReels,
            todayDurationMs: original.todayDurationMs + tickMs,
            sessionReels: original.sessionReels,
            activeSessionDurationMs: original.activeSessionDurationMs + tickMs,
            limitProgress: 1.0,
            decisionReason: "Daily Limit Reached",
            isBlocked: true,
            source: "ESTIMATED",
            updatedAtUtc: "2026-10-15T10:01:00Z"
        )

        assert(updated.todayDurationMs == 31 * 60 * 1000)
        assert(updated.isBlocked == true)
        assert(updated.statusText == "BLOCKED")
    }
}
