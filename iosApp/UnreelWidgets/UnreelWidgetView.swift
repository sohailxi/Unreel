import SwiftUI
import WidgetKit

/**
 * Timeline entry for WidgetKit widgets.
 */
struct UnreelWidgetEntry: TimelineEntry {
    let date: Date
    let snapshot: WidgetUsageSnapshot
}

/**
 * Root view routing to the correct widget layout based on widgetFamily.
 */
struct UnreelWidgetEntryView: View {
    @Environment(\.widgetFamily) var family
    var entry: UnreelWidgetEntry

    var body: some View {
        switch family {
        case .systemSmall:
            SmallWidgetView(snapshot: entry.snapshot)
        case .systemMedium:
            MediumWidgetView(snapshot: entry.snapshot)
        case .accessoryRectangular:
            AccessoryRectangularView(snapshot: entry.snapshot)
        case .accessoryCircular:
            AccessoryCircularView(snapshot: entry.snapshot)
        default:
            SmallWidgetView(snapshot: entry.snapshot)
        }
    }
}

/**
 * Small Home Screen Widget Layout ($2\times2$ cell).
 */
struct SmallWidgetView: View {
    let snapshot: WidgetUsageSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "film.stack.fill")
                    .foregroundColor(.indigo)
                    .font(.subheadline)
                Text("Unreel")
                    .font(.caption.weight(.bold))
                Spacer()
                Text(snapshot.source)
                    .font(.system(size: 8, weight: .heavy))
                    .padding(.horizontal, 4)
                    .padding(.vertical, 2)
                    .background(Color.orange.opacity(0.2))
                    .foregroundColor(.orange)
                    .clipShape(Capsule())
            }

            Spacer()

            VStack(alignment: .leading, spacing: 2) {
                Text("\(snapshot.todayReels)")
                    .font(.system(size: 32, weight: .bold, design: .rounded))
                    .foregroundColor(.primary)
                Text("reels today")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            Spacer()

            VStack(alignment: .leading, spacing: 4) {
                if let progress = snapshot.limitProgress {
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 2)
                                .fill(Color.secondary.opacity(0.2))
                                .frame(height: 4)
                            RoundedRectangle(cornerRadius: 2)
                                .fill(snapshot.isBlocked ? Color.red : (progress >= 0.85 ? Color.orange : Color.indigo))
                                .frame(width: geo.size.width * CGFloat(min(1.0, max(0.0, progress))), height: 4)
                        }
                    }
                    .frame(height: 4)
                }

                HStack {
                    Text(snapshot.formattedDuration)
                        .font(.caption2.weight(.medium))
                        .foregroundColor(.secondary)
                    Spacer()
                    Text(snapshot.statusText)
                        .font(.system(size: 9, weight: .bold))
                        .foregroundColor(snapshot.isBlocked ? .red : (snapshot.limitProgress ?? 0 >= 0.85 ? .orange : .indigo))
                }
            }
        }
        .padding(12)
        .containerBackground(for: .widget) {
            Color(.secondarySystemBackground)
        }
    }
}

/**
 * Medium Home Screen Widget Layout ($4\times2$ cell).
 */
struct MediumWidgetView: View {
    let snapshot: WidgetUsageSnapshot

    var body: some View {
        VStack(spacing: 10) {
            // Header
            HStack {
                HStack(spacing: 6) {
                    Image(systemName: "film.stack.fill")
                        .foregroundColor(.indigo)
                        .font(.headline)
                    Text("Unreel")
                        .font(.headline.weight(.bold))
                    Text(snapshot.source)
                        .font(.system(size: 9, weight: .heavy))
                        .padding(.horizontal, 5)
                        .padding(.vertical, 2)
                        .background(Color.orange.opacity(0.2))
                        .foregroundColor(.orange)
                        .clipShape(Capsule())
                }
                Spacer()
                Text(snapshot.date)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }

            Divider()

            // Metrics row
            HStack(spacing: 16) {
                // Left Column: Daily reels & duration
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(snapshot.todayReels)")
                        .font(.system(size: 34, weight: .bold, design: .rounded))
                        .foregroundColor(.primary)
                    Text("Today's Reels")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    HStack(spacing: 4) {
                        Image(systemName: "clock")
                            .font(.system(size: 10))
                            .foregroundColor(.secondary)
                        Text(snapshot.formattedDuration)
                            .font(.caption.weight(.semibold))
                            .foregroundColor(.secondary)
                    }
                    .padding(.top, 2)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                // Vertical separator
                Rectangle()
                    .fill(Color.secondary.opacity(0.2))
                    .frame(width: 1)
                    .padding(.vertical, 4)

                // Right Column: Session & Limit Progress
                VStack(alignment: .leading, spacing: 6) {
                    HStack {
                        Image(systemName: "flame.fill")
                            .font(.caption2)
                            .foregroundColor(.orange)
                        Text("Session: \(snapshot.sessionReels) reels")
                            .font(.caption.weight(.semibold))
                            .foregroundColor(.primary)
                    }

                    if let progress = snapshot.limitProgress {
                        VStack(alignment: .leading, spacing: 3) {
                            HStack {
                                Text("Daily Limit")
                                    .font(.caption2)
                                    .foregroundColor(.secondary)
                                Spacer()
                                Text(snapshot.formattedProgress)
                                    .font(.caption2.weight(.bold))
                                    .foregroundColor(snapshot.isBlocked ? .red : (progress >= 0.85 ? .orange : .indigo))
                            }
                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    RoundedRectangle(cornerRadius: 3)
                                        .fill(Color.secondary.opacity(0.2))
                                        .frame(height: 6)
                                    RoundedRectangle(cornerRadius: 3)
                                        .fill(snapshot.isBlocked ? Color.red : (progress >= 0.85 ? Color.orange : Color.indigo))
                                        .frame(width: geo.size.width * CGFloat(min(1.0, max(0.0, progress))), height: 6)
                                }
                            }
                            .frame(height: 6)
                        }
                    }

                    HStack {
                        Spacer()
                        Text(snapshot.statusText)
                            .font(.caption2.weight(.bold))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(
                                snapshot.isBlocked ? Color.red.opacity(0.15) :
                                    (snapshot.limitProgress ?? 0 >= 0.85 ? Color.orange.opacity(0.15) : Color.indigo.opacity(0.15))
                            )
                            .foregroundColor(
                                snapshot.isBlocked ? .red :
                                    (snapshot.limitProgress ?? 0 >= 0.85 ? .orange : .indigo)
                            )
                            .clipShape(Capsule())
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
        }
        .padding(14)
        .containerBackground(for: .widget) {
            Color(.secondarySystemBackground)
        }
    }
}

/**
 * Lock Screen Rectangular Complication.
 */
struct AccessoryRectangularView: View {
    let snapshot: WidgetUsageSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            HStack(spacing: 4) {
                Image(systemName: "film.stack.fill")
                    .font(.caption2)
                Text("Unreel (EST)")
                    .font(.caption2.weight(.bold))
            }
            Text("\(snapshot.todayReels) reels • \(snapshot.formattedDuration)")
                .font(.headline)
            Text(snapshot.statusText)
                .font(.caption2)
                .foregroundColor(.secondary)
        }
    }
}

/**
 * Lock Screen Circular Complication.
 */
struct AccessoryCircularView: View {
    let snapshot: WidgetUsageSnapshot

    var body: some View {
        ZStack {
            AccessoryWidgetBackground()
            VStack(spacing: 1) {
                Image(systemName: "film.stack")
                    .font(.system(size: 11))
                Text("\(snapshot.todayReels)")
                    .font(.system(size: 13, weight: .bold, design: .rounded))
            }
        }
    }
}
