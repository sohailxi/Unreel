import SwiftUI
import Shared

struct StatsView: View {
    @ObservedObject var viewModel: StatsObservable
    @State private var selectedDays: Int32 = 7

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    if let state = viewModel.state {
                        // Header Row
                        HStack(alignment: .center) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Usage Trends")
                                    .font(.system(.title2, design: .rounded, weight: .bold))
                                Text("Historical scroll analytics")
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            HStack(spacing: 8) {
                                HStack(spacing: 4) {
                                    Image(systemName: "gauge.with.dots.needle.50percent")
                                        .symbolRenderingMode(.hierarchical)
                                    Text(state.dataSource.name)
                                        .font(.caption)
                                        .fontWeight(.semibold)
                                }
                                .padding(.horizontal, 10)
                                .padding(.vertical, 6)
                                .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
                                .foregroundStyle(.secondary)

                                Button(action: {
                                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                    viewModel.refresh()
                                }) {
                                    Image(systemName: "arrow.clockwise")
                                        .font(.system(size: 14, weight: .semibold))
                                        .padding(8)
                                        .background(Color(uiColor: .tertiarySystemFill), in: Circle())
                                }
                                .buttonStyle(.plain)
                            }
                        }

                        // Time Range Picker
                        Picker("Range", selection: $selectedDays) {
                            Text("7d").tag(Int32(7))
                            Text("14d").tag(Int32(14))
                            Text("30d").tag(Int32(30))
                        }
                        .pickerStyle(.segmented)
                        .onChange(of: selectedDays) { _, newDays in
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            viewModel.selectTimeRange(days: newDays)
                        }

                        // Trajectory Trend Card
                        TrendCardView(trend: state.trend)

                        // Averages Summary Card
                        AveragesCardView(
                            avgReels: Int(state.averageDailyReels),
                            avgDurationMs: state.averageDailyDurationMs
                        )

                        // Reels by App Section (Period Breakdown by Ranking)
                        if !state.appBreakdown.isEmpty {
                            VStack(alignment: .leading, spacing: 12) {
                                HStack(spacing: 6) {
                                    Image(systemName: "chart.bar.xaxis")
                                        .font(.subheadline)
                                        .foregroundStyle(Color.accentColor)
                                    Text("App Ranking (\(selectedDays)d)")
                                        .font(.headline)
                                        .foregroundStyle(.primary)
                                }

                                ForEach(Array(state.appBreakdown.enumerated()), id: \.element.appId) { index, item in
                                    let rank = index + 1
                                    let share = state.totalReels > 0 ? min(max(Double(item.reels) / Double(state.totalReels), 0.0), 1.0) : 0.0
                                    let percentText = "\(Int(share * 100))%"
                                    let displayName = TrackedAppCatalog.shared.getDisplayName(packageOrId: item.appId)
                                    let accentColor = appAccentColor(item.appId, rank: rank)
                                    let badgeColor = rankBadgeColor(rank)

                                    VStack(spacing: 8) {
                                        HStack {
                                            HStack(spacing: 8) {
                                                Text("#\(rank)")
                                                    .font(.system(size: 11, weight: .bold))
                                                    .padding(.horizontal, 6)
                                                    .padding(.vertical, 3)
                                                    .background(badgeColor.opacity(0.18), in: Capsule())
                                                    .foregroundStyle(badgeColor)

                                                VStack(alignment: .leading, spacing: 2) {
                                                    Text(displayName)
                                                        .font(.headline)
                                                        .foregroundStyle(.primary)
                                                    Text(formatDuration(item.durationMs))
                                                        .font(.caption)
                                                        .foregroundStyle(.secondary)
                                                }
                                            }

                                            Spacer()

                                            VStack(alignment: .trailing, spacing: 2) {
                                                Text("\(item.reels) reels")
                                                    .font(.system(.subheadline, design: .rounded, weight: .bold))
                                                    .foregroundStyle(accentColor)
                                                Text(percentText)
                                                    .font(.caption2)
                                                    .foregroundStyle(.secondary)
                                            }
                                        }

                                        GeometryReader { geometry in
                                            ZStack(alignment: .leading) {
                                                Capsule()
                                                    .fill(Color(uiColor: .tertiarySystemFill))
                                                    .frame(height: 6)
                                                Capsule()
                                                    .fill(accentColor)
                                                    .frame(width: max(geometry.size.width * CGFloat(share), 0), height: 6)
                                            }
                                        }
                                        .frame(height: 6)
                                    }
                                    .padding(14)
                                    .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                                }
                            }
                        }

                        // Daily Breakdown Section
                        VStack(alignment: .leading, spacing: 12) {
                            HStack(spacing: 6) {
                                Image(systemName: "calendar")
                                    .font(.subheadline)
                                    .foregroundStyle(Color.accentColor)
                                Text("Daily Breakdown")
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                            }

                            if state.dailyHistory.isEmpty {
                                HStack(spacing: 8) {
                                    Image(systemName: "chart.line.downtrend.xyaxis")
                                        .font(.title3)
                                        .foregroundStyle(.secondary)
                                    Text("No history recorded yet.")
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(16)
                                .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                            } else {
                                ForEach(state.dailyHistory, id: \.localDate) { daily in
                                    DailyRowView(daily: daily)
                                }
                            }
                        }
                    } else {
                        ProgressView()
                            .padding(.top, 40)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .background(Color(uiColor: .systemGroupedBackground))
            .navigationTitle("Trends")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func formatDuration(_ ms: Int64) -> String {
        let totalSeconds = ms / 1000
        let hours = totalSeconds / 3600
        let minutes = (totalSeconds % 3600) / 60
        let seconds = totalSeconds % 60

        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else if minutes > 0 {
            return "\(minutes)m"
        } else {
            return "\(seconds)s"
        }
    }

    private func rankBadgeColor(_ rank: Int) -> Color {
        switch rank {
        case 1: return Color(red: 0.96, green: 0.65, blue: 0.14) // Gold
        case 2: return Color(red: 0.55, green: 0.61, blue: 0.68) // Silver
        case 3: return Color(red: 0.80, green: 0.50, blue: 0.35) // Bronze
        default: return Color.secondary
        }
    }

    private func appAccentColor(_ appId: String, rank: Int) -> Color {
        let lower = appId.lowercased()
        if lower.contains("instagram") || lower.contains("instander") { return Color(red: 0.88, green: 0.19, blue: 0.42) }
        if lower.contains("youtube") { return Color(red: 1.0, green: 0.0, blue: 0.0) }
        if lower.contains("musically") || lower.contains("tiktok") { return Color(red: 0.0, green: 0.81, blue: 0.79) }
        if lower.contains("snapchat") { return Color(red: 1.0, green: 0.73, blue: 0.0) }
        if lower.contains("facebook") || lower.contains("katana") { return Color(red: 0.09, green: 0.47, blue: 0.95) }
        if lower.contains("reddit") { return Color(red: 1.0, green: 0.27, blue: 0.0) }
        switch rank {
        case 1: return Color.accentColor
        case 2: return Color.teal
        case 3: return Color.orange
        default: return Color.secondary
        }
    }
}

// MARK: - Subviews

private struct TrendCardView: View {
    let trend: TrendResult

    var trendColor: Color {
        switch trend.direction {
        case .improving: return .green
        case .slipping: return .red
        case .steady: return .primary
        default: return .secondary
        }
    }

    var trendIcon: String {
        switch trend.direction {
        case .improving: return "arrow.down.right.circle.fill"
        case .slipping: return "arrow.up.right.circle.fill"
        case .steady: return "arrow.right.circle.fill"
        default: return "equal.circle.fill"
        }
    }

    var trendTitle: String {
        if trend.confidence == .low {
            return "Not enough data"
        }
        let sign = trend.percentChange > 0 ? "+" : ""
        let formatted = String(format: "\(sign)%.1f%%", trend.percentChange)
        switch trend.direction {
        case .improving: return "Improving (\(formatted))"
        case .slipping: return "Slipping (\(formatted))"
        case .steady: return "Steady (\(formatted))"
        default: return "Steady"
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("14-Day Trajectory")
                .font(.caption)
                .foregroundStyle(.secondary)

            HStack {
                HStack(spacing: 8) {
                    Image(systemName: trendIcon)
                        .symbolRenderingMode(.hierarchical)
                        .font(.title2)
                        .foregroundStyle(trendColor)
                    Text(trendTitle)
                        .font(.system(.title3, design: .rounded, weight: .bold))
                        .foregroundStyle(trendColor)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                }

                Spacer()

                HStack(spacing: 4) {
                    Image(systemName: trend.confidence == .high ? "checkmark.seal.fill" : "questionmark.circle.fill")
                        .font(.caption2)
                    Text(trend.confidence.name)
                        .font(.caption2)
                        .fontWeight(.semibold)
                }
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
                .foregroundStyle(.secondary)
            }
        }
        .padding(20)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

private struct AveragesCardView: View {
    let avgReels: Int
    let avgDurationMs: Int64

    var body: some View {
        HStack {
            VStack(spacing: 4) {
                HStack(spacing: 6) {
                    Image(systemName: "play.rectangle.fill")
                        .symbolRenderingMode(.hierarchical)
                        .font(.headline)
                        .foregroundStyle(Color.accentColor)
                    Text("\(avgReels)")
                        .font(.system(.title2, design: .rounded, weight: .bold))
                        .foregroundStyle(Color.accentColor)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                }
                Text("Daily Avg Reels")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)

            Divider()
                .frame(height: 36)

            VStack(spacing: 4) {
                HStack(spacing: 6) {
                    Image(systemName: "clock.fill")
                        .symbolRenderingMode(.hierarchical)
                        .font(.headline)
                        .foregroundStyle(.primary)
                    Text(formatDuration(avgDurationMs))
                        .font(.system(.title2, design: .rounded, weight: .bold))
                        .foregroundStyle(.primary)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                }
                Text("Daily Avg Time")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            .frame(maxWidth: .infinity)
        }
        .padding(20)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }

    private func formatDuration(_ ms: Int64) -> String {
        let totalSeconds = ms / 1000
        let hours = totalSeconds / 3600
        let minutes = (totalSeconds % 3600) / 60
        let seconds = totalSeconds % 60

        if hours > 0 {
            return "\(hours)h \(minutes)m"
        } else if minutes > 0 {
            return "\(minutes)m"
        } else {
            return "\(seconds)s"
        }
    }
}

private struct DailyRowView: View {
    let daily: DailyUsage

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 6) {
                    Image(systemName: "calendar")
                        .font(.caption2)
                        .foregroundStyle(Color.accentColor)
                    Text(daily.localDate.description)
                        .font(.headline)
                        .foregroundStyle(.primary)
                }

                HStack(spacing: 12) {
                    HStack(spacing: 3) {
                        Image(systemName: "repeat")
                            .font(.system(size: 10))
                        Text("\(daily.sessions) sessions")
                            .font(.caption)
                    }
                    .foregroundStyle(.secondary)

                    HStack(spacing: 3) {
                        Image(systemName: "arrow.up.forward.app")
                            .font(.system(size: 10))
                        Text("\(daily.opens) opens")
                            .font(.caption)
                    }
                    .foregroundStyle(.secondary)
                }
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text("\(daily.reels) reels")
                    .font(.system(.subheadline, design: .rounded, weight: .bold))
                    .foregroundStyle(Color.accentColor)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
                Text(formatDwell(daily.durationMs))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
            }
        }
        .padding(14)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
    }

    private func formatDwell(_ ms: Int64) -> String {
        let s = ms / 1000
        let m = s / 60
        let h = m / 60
        if h > 0 {
            return "\(h)h \(m % 60)m"
        } else if m > 0 {
            return "\(m)m"
        } else {
            return "\(s)s"
        }
    }
}
