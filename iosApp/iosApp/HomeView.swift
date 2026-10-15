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
                                Text("Tracked app: \(state.selectedAppId ?? "All Apps")")
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

                        // Active Session Card
                        if let session = state.activeSession {
                            ActiveSessionCardView(session: session)
                        }

                        // Focus Score Card
                        if let focusScore = state.focusScore {
                            FocusScoreCardView(score: Int(focusScore.value))
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
