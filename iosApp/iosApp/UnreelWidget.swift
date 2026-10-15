import WidgetKit
import SwiftUI

/**
 * Timeline provider for Unreel Home Screen widgets.
 * Decodes the latest UsageSnapshot from the shared App Group.
 */
struct UnreelTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> UnreelWidgetEntry {
        UnreelWidgetEntry(date: Date(), snapshot: .placeholder)
    }

    func getSnapshot(in context: Context, completion: @escaping (UnreelWidgetEntry) -> Void) {
        let snapshot = SnapshotReader.readLatestSnapshot()
        completion(UnreelWidgetEntry(date: Date(), snapshot: snapshot))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<UnreelWidgetEntry>) -> Void) {
        let snapshot = SnapshotReader.readLatestSnapshot()
        let entry = UnreelWidgetEntry(date: Date(), snapshot: snapshot)
        // Refresh every 15 minutes, or when refreshed via WidgetCenter.shared.reloadAllTimelines()
        let nextUpdate = Calendar.current.date(byAdding: .minute, value: 15, to: Date()) ?? Date().addingTimeInterval(900)
        let timeline = Timeline(entries: [entry], policy: .after(nextUpdate))
        completion(timeline)
    }
}

/**
 * Main Unreel Widget configuration.
 */
struct UnreelWidget: Widget {
    let kind: String = "app.unreel.ios.UnreelWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: UnreelTimelineProvider()) { entry in
            UnreelWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("Unreel Usage Tracker")
        .description("Track your estimated reel counts, scroll duration, and daily limits in real-time.")
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryRectangular, .accessoryCircular])
    }
}
