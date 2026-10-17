import SwiftUI
import ActivityKit
import WidgetKit

/**
 * ActivityKit Live Activity widget displaying real-time scroll session metrics
 * across Dynamic Island (Expanded, Compact, Minimal) and Lock Screen banners.
 */
struct UnreelLiveActivity: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: UnreelActivityAttributes.self) { context in
            // Lock Screen Banner & StandBy Presentation
            LiveActivityBannerView(
                appName: context.attributes.appName,
                state: context.state
            )
            .activityBackgroundTint(Color.black.opacity(0.85))
            .activitySystemActionForegroundColor(Color.white)
        } dynamicIsland: { context in
            DynamicIsland {
                // Expanded Dynamic Island
                DynamicIslandExpandedRegion(.leading) {
                    HStack(spacing: 8) {
                        Image(systemName: "film.stack.fill")
                            .font(.title3)
                            .foregroundColor(Color.indigo)
                        VStack(alignment: .leading, spacing: 2) {
                            Text("\(context.state.sessionReels)")
                                .font(.system(size: 22, weight: .bold, design: .rounded))
                                .foregroundColor(.primary)
                            Text("Session")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.leading, 8)
                }

                DynamicIslandExpandedRegion(.trailing) {
                    VStack(alignment: .trailing, spacing: 2) {
                        Text("\(context.state.sessionDurationMinutes)m")
                            .font(.system(size: 20, weight: .bold, design: .rounded))
                            .foregroundColor(.primary)
                        Text("Duration")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                    .padding(.trailing, 8)
                }

                DynamicIslandExpandedRegion(.center) {
                    HStack(spacing: 4) {
                        Text(context.attributes.appName)
                            .font(.caption.weight(.semibold))
                            .lineLimit(1)
                        Text(context.state.source)
                            .font(.system(size: 9, weight: .bold))
                            .padding(.horizontal, 5)
                            .padding(.vertical, 2)
                            .background(Color.orange.opacity(0.2))
                            .foregroundColor(.orange)
                            .clipShape(Capsule())
                    }
                }

                DynamicIslandExpandedRegion(.bottom) {
                    VStack(spacing: 6) {
                        if let progress = context.state.limitProgress {
                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    RoundedRectangle(cornerRadius: 3)
                                        .fill(Color.secondary.opacity(0.2))
                                        .frame(height: 6)
                                    RoundedRectangle(cornerRadius: 3)
                                        .fill(
                                            context.state.isBlocked ? Color.red :
                                                (context.state.isNearingLimit ? Color.orange : Color.indigo)
                                        )
                                        .frame(width: geo.size.width * CGFloat(min(1.0, max(0.0, progress))), height: 6)
                                }
                            }
                            .frame(height: 6)
                        }

                        HStack {
                            Text("Today: \(context.state.todayReels) reels")
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Spacer()
                            Text(
                                context.state.isBlocked ? "BLOCKED" :
                                    (context.state.isNearingLimit ? "Nearing limit" : "Active")
                            )
                            .font(.caption2.weight(.bold))
                            .foregroundColor(
                                context.state.isBlocked ? .red :
                                    (context.state.isNearingLimit ? .orange : .green)
                            )
                        }
                    }
                    .padding(.horizontal, 10)
                    .padding(.bottom, 6)
                }
            } compactLeading: {
                HStack(spacing: 3) {
                    Image(systemName: "film.stack")
                        .foregroundColor(.indigo)
                        .font(.caption2)
                    Text("\(context.state.sessionReels)")
                        .font(.system(size: 13, weight: .bold, design: .rounded))
                }
            } compactTrailing: {
                Text("\(context.state.sessionDurationMinutes)m")
                    .font(.system(size: 13, weight: .semibold, design: .rounded))
                    .foregroundColor(context.state.isBlocked ? .red : (context.state.isNearingLimit ? .orange : .primary))
            } minimal: {
                Image(systemName: "film.stack.fill")
                    .foregroundColor(.indigo)
            }
        }
    }
}

/**
 * Lock Screen & StandBy banner for Live Activity.
 */
struct LiveActivityBannerView: View {
    let appName: String
    let state: UnreelActivityAttributes.ContentState

    var body: some View {
        VStack(spacing: 12) {
            HStack(alignment: .center) {
                HStack(spacing: 8) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 8)
                            .fill(Color.indigo.opacity(0.2))
                            .frame(width: 32, height: 32)
                        Image(systemName: "film.stack.fill")
                            .foregroundColor(.indigo)
                            .font(.system(size: 16))
                    }
                    VStack(alignment: .leading, spacing: 2) {
                        Text(appName)
                            .font(.headline)
                            .foregroundColor(.white)
                        HStack(spacing: 4) {
                            Text("Unreel Live")
                                .font(.caption2)
                                .foregroundColor(.gray)
                            Text(state.source)
                                .font(.system(size: 8, weight: .heavy))
                                .padding(.horizontal, 4)
                                .padding(.vertical, 1)
                                .background(Color.orange.opacity(0.25))
                                .foregroundColor(.orange)
                                .clipShape(Capsule())
                        }
                    }
                }

                Spacer()

                VStack(alignment: .trailing, spacing: 2) {
                    Text("\(state.sessionReels)")
                        .font(.system(size: 26, weight: .bold, design: .rounded))
                        .foregroundColor(.white)
                    Text("session reels")
                        .font(.caption2)
                        .foregroundColor(.gray)
                }
            }

            // Progress bar and status indicator
            VStack(spacing: 6) {
                if let progress = state.limitProgress {
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            RoundedRectangle(cornerRadius: 3)
                                .fill(Color.white.opacity(0.15))
                                .frame(height: 6)
                            RoundedRectangle(cornerRadius: 3)
                                .fill(
                                    state.isBlocked ? Color.red :
                                        (state.isNearingLimit ? Color.orange : Color.indigo)
                                )
                                .frame(width: geo.size.width * CGFloat(min(1.0, max(0.0, progress))), height: 6)
                        }
                    }
                    .frame(height: 6)
                }

                HStack {
                    Text("Today: \(state.todayReels) reels • \(state.sessionDurationMinutes)m session")
                        .font(.caption2)
                        .foregroundColor(.gray)
                    Spacer()
                    Text(
                        state.isBlocked ? "BLOCKED" :
                            (state.isNearingLimit ? "Nearing Limit" : "Tracking Active")
                    )
                    .font(.caption2.weight(.bold))
                    .foregroundColor(
                        state.isBlocked ? .red :
                            (state.isNearingLimit ? .orange : .green)
                    )
                }
            }
        }
        .padding(16)
        .background(Color.black.opacity(0.92))
    }
}
