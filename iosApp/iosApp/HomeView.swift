import SwiftUI
import Shared

struct HomeView: View {
    @ObservedObject var viewModel: HomeObservable

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    if let state = viewModel.state {
                        // Header Status Row
                        HStack(alignment: .center) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text("Today's Scroll")
                                    .font(.system(.title2, design: .rounded, weight: .bold))
                                Text("All short-video feeds & reels")
                                    .font(.subheadline)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            HStack(spacing: 8) {
                                // iOS is estimated per project spec
                                HStack(spacing: 4) {
                                    Image(systemName: "gauge.with.dots.needle.50percent")
                                        .symbolRenderingMode(.hierarchical)
                                    Text(state.source.name)
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

                        // Restriction Banner
                        if isRestricted(decision: state.currentDecision) {
                            RestrictionBannerView(
                                decision: state.currentDecision,
                                onBypass: {
                                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                                    viewModel.requestBypass()
                                }
                            )
                        }

                        // Today's Usage Card
                        TodayUsageCardView(
                            reels: Int(state.totalReels),
                            durationMs: state.totalDurationMs,
                            goalReels: Int(state.goalReels)
                        )

                        // Reels by App Breakdown
                        if !state.appBreakdown.isEmpty {
                            HomeAppBreakdownCardView(
                                breakdown: state.appBreakdown,
                                totalReels: Int(state.totalReels),
                                selectedAppId: state.selectedAppId,
                                onSelectApp: { appId in
                                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                    viewModel.selectApp(appId: appId)
                                }
                            )
                        }

                        // Active Session Card
                        if let session = state.activeSession {
                            ActiveSessionCardView(session: session)
                        }

                        // Focus Score Card
                        if let focusScore = state.focusScore {
                            FocusScoreCardView(score: Int(focusScore.value))
                        }

                        // Live Surfaces & Widget Status
                        LiveSurfacesCardView(state: state)
                    } else {
                        ProgressView()
                            .padding(.top, 40)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
            }
            .background(Color(uiColor: .systemGroupedBackground))
            .navigationTitle("Unreel")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    private func isRestricted(decision: Decision) -> Bool {
        return decision is DecisionBlock || decision is DecisionFriction
    }
}

// MARK: - Subviews

private struct RestrictionBannerView: View {
    let decision: Decision
    let onBypass: () -> Void

    var body: some View {
        let (title, reason, canBypass) = parseDecision(decision)

        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 8) {
                Image(systemName: "exclamationmark.octagon.fill")
                    .font(.title3)
                    .foregroundStyle(.red)
                Text(title)
                    .font(.headline)
                    .fontWeight(.bold)
                    .foregroundStyle(.primary)
            }

            Text(reason)
                .font(.subheadline)
                .foregroundStyle(.secondary)

            if canBypass {
                Button(action: onBypass) {
                    HStack(spacing: 6) {
                        Image(systemName: "key.fill")
                            .font(.caption)
                        Text("Use 5-min Emergency Pass")
                            .font(.subheadline)
                            .fontWeight(.semibold)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.red.opacity(0.15), in: Capsule())
                    .foregroundStyle(.red)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(Color.red.opacity(0.3), lineWidth: 1)
        )
    }

    private func parseDecision(_ decision: Decision) -> (String, String, Bool) {
        if let block = decision as? DecisionBlock {
            return ("Scroll Limit Enforced", block.reason, block.canBypass)
        } else if let friction = decision as? DecisionFriction {
            return ("Take a Breath", friction.reason, false)
        } else if let nudge = decision as? DecisionNudge {
            return ("Gentle Nudge", nudge.reason, false)
        }
        return ("", "", false)
    }
}

private struct TodayUsageCardView: View {
    let reels: Int
    let durationMs: Int64
    let goalReels: Int

    var progress: Double {
        guard goalReels > 0 else { return 0.0 }
        return min(max(Double(reels) / Double(goalReels), 0.0), 1.0)
    }

    var body: some View {
        VStack(spacing: 18) {
            HStack(alignment: .lastTextBaseline) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Image(systemName: "play.rectangle.fill")
                            .symbolRenderingMode(.hierarchical)
                            .font(.title2)
                            .foregroundStyle(Color.accentColor)
                        Text("\(reels)")
                            .font(.system(size: 44, weight: .black, design: .rounded))
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }
                    Text("reels watched")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 4) {
                    HStack(spacing: 6) {
                        Image(systemName: "clock.fill")
                            .symbolRenderingMode(.hierarchical)
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                        Text(formatDuration(durationMs))
                            .font(.system(.title3, design: .rounded, weight: .bold))
                            .foregroundStyle(.primary)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                    }
                    Text("total time")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            VStack(spacing: 6) {
                HStack {
                    HStack(spacing: 4) {
                        Image(systemName: "flag.fill")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                        Text("Daily Goal (\(goalReels) reels)")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                    Spacer()
                    Text("\(Int(progress * 100))%")
                        .font(.caption)
                        .fontWeight(.semibold)
                        .foregroundStyle(.secondary)
                }

                GeometryReader { geometry in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(Color(uiColor: .tertiarySystemFill))
                            .frame(height: 10)
                        Capsule()
                            .fill(progress >= 1.0 ? Color.red : Color.accentColor)
                            .frame(width: max(geometry.size.width * CGFloat(progress), 0), height: 10)
                    }
                }
                .frame(height: 10)
            }
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
        } else if minutes > 0 && seconds > 0 {
            return "\(minutes)m \(seconds)s"
        } else if minutes > 0 {
            return "\(minutes)m"
        } else if seconds > 0 {
            return "\(seconds)s"
        }
        return "0m"
    }
}

private struct ActiveSessionCardView: View {
    let session: Session

    var body: some View {
        HStack(spacing: 14) {
            ZStack {
                Circle()
                    .fill(Color.teal.opacity(0.2))
                    .frame(width: 40, height: 40)
                Image(systemName: "record.circle.fill")
                    .symbolRenderingMode(.hierarchical)
                    .foregroundStyle(.teal)
                    .font(.title3)
            }

            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Image(systemName: "iphone.gen3")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                    Text("Active Session (\(session.appId))")
                        .font(.headline)
                        .foregroundStyle(.primary)
                }

                HStack(spacing: 4) {
                    Image(systemName: "timer")
                        .font(.caption2)
                        .foregroundStyle(.secondary)
                    Text("\(session.reelCount) reels • \(formatDwell(session.durationMs))")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
            }
            Spacer()
        }
        .padding(16)
        .background(Color.teal.opacity(0.12), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }

    private func formatDwell(_ ms: Int64) -> String {
        let s = ms / 1000
        let m = s / 60
        let remS = s % 60
        return m > 0 ? "\(m)m \(remS)s" : "\(remS)s"
    }
}

private struct FocusScoreCardView: View {
    let score: Int

    var scoreColor: Color {
        if score >= 80 { return .green }
        if score >= 50 { return .orange }
        return .red
    }

    var scoreDescription: String {
        if score >= 80 { return "Excellent focus today" }
        if score >= 50 { return "Moderate scroll habits" }
        return "High distraction detected"
    }

    var body: some View {
        HStack(spacing: 14) {
            Image(systemName: "brain.head.profile")
                .symbolRenderingMode(.hierarchical)
                .font(.system(size: 32))
                .foregroundStyle(Color.accentColor)

            VStack(alignment: .leading, spacing: 3) {
                Text("Focus Score")
                    .font(.headline)
                    .foregroundStyle(.primary)
                Text(scoreDescription)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            Text("\(score)")
                .font(.system(size: 36, weight: .black, design: .rounded))
                .foregroundStyle(scoreColor)
        }
        .padding(20)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

private struct LiveSurfacesCardView: View {
    let state: HomeState
    @ObservedObject var activityManager = ActivityKitManager.shared

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "dot.radiowaves.left.and.right")
                        .foregroundColor(.indigo)
                        .font(.title3)
                    Text("Live Surfaces")
                        .font(.headline.weight(.bold))
                }
                Spacer()
                Text("iOS 17+")
                    .font(.caption2.weight(.medium))
                    .foregroundColor(.secondary)
            }

            VStack(spacing: 10) {
                // Live Activity status row
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "iphone.gen3")
                            .foregroundColor(.secondary)
                            .font(.subheadline)
                        VStack(alignment: .leading, spacing: 1) {
                            Text("Dynamic Island & Lock Screen")
                                .font(.subheadline.weight(.semibold))
                            Text(activityManager.isLiveActivityActive ? "Active session tracking" : "Idle (starts during scroll)")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                    }
                    Spacer()
                    Circle()
                        .fill(activityManager.isLiveActivityActive ? Color.green : Color.secondary.opacity(0.4))
                        .frame(width: 8, height: 8)
                }

                // WidgetKit status row
                HStack {
                    HStack(spacing: 8) {
                        Image(systemName: "square.grid.2x2")
                            .foregroundColor(.secondary)
                            .font(.subheadline)
                        VStack(alignment: .leading, spacing: 1) {
                            Text("Home Screen Widgets")
                                .font(.subheadline.weight(.semibold))
                            Text("Small & Medium WidgetKit timeline")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                    }
                    Spacer()
                    Button(action: {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        activityManager.reloadWidgets()
                    }) {
                        Text("Refresh")
                            .font(.caption2.weight(.bold))
                            .padding(.horizontal, 10)
                            .padding(.vertical, 4)
                            .background(Color.indigo.opacity(0.15))
                            .foregroundColor(.indigo)
                            .clipShape(Capsule())
                    }
                }
            }

            Divider()

            // Test Live Activity Button
            Button(action: {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                if activityManager.isLiveActivityActive {
                    activityManager.endLiveActivity()
                } else {
                    let progress = state.goalReels > 0 ? Float(state.totalReels) / Float(state.goalReels) : nil
                    activityManager.startLiveActivity(
                        appName: "Instagram Reels",
                        sessionReels: Int(state.activeSession?.reelCount ?? 8),
                        todayReels: Int(state.totalReels),
                        sessionDurationMinutes: Int(max(1, state.totalDurationMs / 60_000)),
                        limitProgress: progress
                    )
                }
            }) {
                HStack {
                    Image(systemName: activityManager.isLiveActivityActive ? "stop.fill" : "play.fill")
                    Text(activityManager.isLiveActivityActive ? "Stop Live Activity" : "Simulate Live Activity")
                        .font(.subheadline.weight(.semibold))
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(
                    activityManager.isLiveActivityActive ? Color.red.opacity(0.15) : Color.indigo.opacity(0.15)
                )
                .foregroundColor(activityManager.isLiveActivityActive ? .red : .indigo)
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            }
        }
        .padding(18)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

private struct HomeAppBreakdownCardView: View {
    let breakdown: [DailyUsage]
    let totalReels: Int
    let selectedAppId: String?
    let onSelectApp: (String?) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                HStack(spacing: 8) {
                    Image(systemName: "square.stack.3d.up.fill")
                        .foregroundStyle(Color.accentColor)
                        .font(.title3)
                    VStack(alignment: .leading, spacing: 1) {
                        Text("Reels by App")
                            .font(.headline.weight(.bold))
                        Text("Where you watched reels today")
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
                Spacer()
                if selectedAppId != nil {
                    Button(action: { onSelectApp(nil) }) {
                        Text("Show all")
                            .font(.caption2.weight(.semibold))
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
                            .foregroundStyle(.secondary)
                    }
                    .buttonStyle(.plain)
                }
            }

            VStack(spacing: 10) {
                ForEach(Array(breakdown.enumerated()), id: \.element.appId) { index, item in
                    let rank = index + 1
                    let isSelected = selectedAppId == item.appId
                    let share = totalReels > 0 ? min(max(Double(item.reels) / Double(totalReels), 0.0), 1.0) : 0.0
                    let percentText = "\(Int(share * 100))%"
                    let displayName = TrackedAppCatalog.shared.getDisplayName(packageOrId: item.appId)
                    let accentColor = appAccentColor(item.appId, rank: rank)
                    let badgeColor = rankBadgeColor(rank)

                    Button(action: {
                        onSelectApp(isSelected ? nil : item.appId)
                    }) {
                        VStack(spacing: 8) {
                            HStack(alignment: .center) {
                                HStack(spacing: 8) {
                                    Text("#\(rank)")
                                        .font(.system(size: 11, weight: .bold))
                                        .padding(.horizontal, 6)
                                        .padding(.vertical, 3)
                                        .background(badgeColor.opacity(0.18), in: Capsule())
                                        .foregroundStyle(badgeColor)

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(displayName)
                                            .font(.subheadline.weight(.semibold))
                                            .foregroundStyle(.primary)
                                            .lineLimit(1)
                                        Text(formatDwell(item.durationMs))
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
                        .padding(12)
                        .background(
                            isSelected ? Color.accentColor.opacity(0.12) : Color(uiColor: .tertiarySystemGroupedBackground),
                            in: RoundedRectangle(cornerRadius: 12, style: .continuous)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                .stroke(isSelected ? Color.accentColor.opacity(0.4) : Color.clear, lineWidth: 1)
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(18)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))
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


