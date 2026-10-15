import SwiftUI
import Shared
import FamilyControls

struct RulesView: View {
    @ObservedObject var viewModel: RulesObservable
    @EnvironmentObject var familyControlsManager: FamilyControlsManager
    @State private var isPickerPresented: Bool = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    if let state = viewModel.state {
                        // Header Row
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Limits & Goals")
                                .font(.system(.title2, design: .rounded, weight: .bold))
                            Text("Control scroll time and set strictness")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)

                        // Screen Time & Shielding Card
                        ScreenTimeProtectionCardView(
                            familyControlsManager: familyControlsManager,
                            isPickerPresented: $isPickerPresented
                        )

                        // Focus Target Goals Card
                        VStack(alignment: .leading, spacing: 16) {
                            HStack(spacing: 6) {
                                Image(systemName: "flag.fill")
                                    .font(.subheadline)
                                    .foregroundStyle(Color.accentColor)
                                Text("Focus Target Goals")
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                            }

                            // Daily Reel Goal Stepper
                            HStack {
                                HStack(spacing: 8) {
                                    Image(systemName: "play.rectangle.fill")
                                        .symbolRenderingMode(.hierarchical)
                                        .font(.title3)
                                        .foregroundStyle(Color.accentColor)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Daily Reel Goal")
                                            .font(.body)
                                            .fontWeight(.semibold)
                                        Text("\(state.goalReels) reels per day")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }

                                Spacer()

                                HStack(spacing: 12) {
                                    Button(action: {
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        let next = max(10, state.goalReels - 10)
                                        viewModel.updateFocusGoalReels(reels: next)
                                    }) {
                                        Image(systemName: "minus.circle.fill")
                                            .font(.title2)
                                            .foregroundStyle(Color(uiColor: .tertiaryLabel))
                                    }

                                    Button(action: {
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        viewModel.updateFocusGoalReels(reels: state.goalReels + 10)
                                    }) {
                                        Image(systemName: "plus.circle.fill")
                                            .font(.title2)
                                            .foregroundStyle(Color.accentColor)
                                    }
                                }
                            }

                            Divider()

                            // Daily Time Goal Stepper
                            HStack {
                                HStack(spacing: 8) {
                                    Image(systemName: "timer")
                                        .symbolRenderingMode(.hierarchical)
                                        .font(.title3)
                                        .foregroundStyle(Color.accentColor)
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text("Daily Time Goal")
                                            .font(.body)
                                            .fontWeight(.semibold)
                                        Text("\(state.goalMinutes) minutes per day")
                                            .font(.caption)
                                            .foregroundStyle(.secondary)
                                    }
                                }

                                Spacer()

                                HStack(spacing: 12) {
                                    Button(action: {
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        let next = max(5, state.goalMinutes - 5)
                                        viewModel.updateFocusGoalMinutes(minutes: next)
                                    }) {
                                        Image(systemName: "minus.circle.fill")
                                            .font(.title2)
                                            .foregroundStyle(Color(uiColor: .tertiaryLabel))
                                    }

                                    Button(action: {
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        viewModel.updateFocusGoalMinutes(minutes: state.goalMinutes + 5)
                                    }) {
                                        Image(systemName: "plus.circle.fill")
                                            .font(.title2)
                                            .foregroundStyle(Color.accentColor)
                                    }
                                }
                            }
                        }
                        .padding(20)
                        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 20, style: .continuous))

                        // Active Rules Section
                        VStack(alignment: .leading, spacing: 12) {
                            HStack(spacing: 6) {
                                Image(systemName: "shield.fill")
                                    .font(.subheadline)
                                    .foregroundStyle(Color.accentColor)
                                Text("Active Rules (\(state.rules.count))")
                                    .font(.headline)
                                    .foregroundStyle(.primary)
                            }

                            if state.rules.isEmpty {
                                HStack(spacing: 10) {
                                    Image(systemName: "checkmark.shield.fill")
                                        .font(.title3)
                                        .foregroundStyle(Color.accentColor)
                                    Text("No custom rules configured yet. Goals above are active.")
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                }
                                .padding(16)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 14, style: .continuous))
                            } else {
                                ForEach(state.rules, id: \.id) { rule in
                                    RuleRowView(rule: rule) { isEnabled in
                                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                        viewModel.toggleRule(id: rule.id, enabled: isEnabled)
                                    }
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
            .navigationTitle("Limits")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

// MARK: - Subviews

private struct RuleRowView: View {
    let rule: LimitRule
    let onToggle: (Bool) -> Void

    var ruleIcon: String {
        switch rule.kind {
        case .dailyReelLimit: return "play.square.stack.fill"
        case .dailyTimeLimit: return "timer"
        case .sessionCap: return "hourglass"
        case .scheduleWindow: return "moon.fill"
        case .openCountLimit: return "lock.shield.fill"
        case .sharedBudget: return "chart.pie.fill"
        default: return "shield.fill"
        }
    }

    var ruleKindTitle: String {
        switch rule.kind {
        case .dailyTimeLimit: return "Daily Time Limit"
        case .dailyReelLimit: return "Daily Reel Limit"
        case .sessionCap: return "Session Cap"
        case .scheduleWindow: return "Schedule Window"
        case .openCountLimit: return "Open Count Limit"
        case .sharedBudget: return "Shared Budget"
        default: return "Custom Rule"
        }
    }

    var ruleParamSummary: String {
        let p = rule.params
        if let reels = p.limitReels {
            return "\(reels) reels limit"
        } else if let minutes = p.limitMinutes {
            return "\(minutes) minutes limit"
        } else if let opens = p.maxOpens {
            return "\(opens) maximum opens"
        }
        return "Custom rule parameter"
    }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: ruleIcon)
                .symbolRenderingMode(.hierarchical)
                .font(.title2)
                .foregroundStyle(Color.accentColor)
                .padding(.top, 2)

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Text(ruleKindTitle)
                        .font(.headline)
                        .foregroundStyle(.primary)

                    HStack(spacing: 3) {
                        Image(systemName: "shield.lefthalf.filled")
                            .font(.system(size: 9))
                        Text(rule.strictness.name)
                            .font(.caption2)
                            .fontWeight(.semibold)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color(uiColor: .tertiarySystemFill), in: Capsule())
                    .foregroundStyle(.secondary)
                }

                Text(ruleParamSummary)
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                Text("Apps: \(rule.appIds.joined(separator: ", "))")
                    .font(.caption)
                    .foregroundStyle(.tertiary)
            }

            Spacer()

            Toggle("", isOn: Binding(
                get: { rule.enabled },
                set: { onToggle($0) }
            ))
            .labelsHidden()
            .tint(Color.accentColor)
        }
        .padding(16)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

private struct ScreenTimeProtectionCardView: View {
    @ObservedObject var familyControlsManager: FamilyControlsManager
    @Binding var isPickerPresented: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack(spacing: 8) {
                Image(systemName: "shield.fill")
                    .font(.subheadline)
                    .foregroundStyle(Color.accentColor)
                Text("Screen Time Shielding")
                    .font(.headline)
                    .foregroundStyle(.primary)
                Spacer()
                if familyControlsManager.isAuthorized {
                    HStack(spacing: 4) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.caption2)
                        Text("Active")
                            .font(.caption2)
                            .fontWeight(.semibold)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color.green.opacity(0.15), in: Capsule())
                    .foregroundStyle(.green)
                }
            }

            Text("Protect your focus by locking short-video apps at the iOS system level when daily limits are reached.")
                .font(.caption)
                .foregroundStyle(.secondary)

            if !familyControlsManager.isAuthorized {
                Button(action: {
                    Task {
                        await familyControlsManager.requestAuthorization()
                    }
                }) {
                    HStack {
                        Image(systemName: "hand.raised.fill")
                        Text("Authorize Screen Time Access")
                    }
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color.accentColor, in: RoundedRectangle(cornerRadius: 12))
                    .foregroundStyle(.white)
                }
            } else {
                Button(action: {
                    isPickerPresented = true
                }) {
                    HStack {
                        Image(systemName: "slider.horizontal.3")
                        Text("Select Apps & Categories to Shield")
                    }
                    .font(.subheadline)
                    .fontWeight(.semibold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color(uiColor: .tertiarySystemFill), in: RoundedRectangle(cornerRadius: 12))
                    .foregroundStyle(.primary)
                }
                .familyActivityPicker(isPresented: $isPickerPresented, selection: $familyControlsManager.activitySelection)
                .onChange(of: familyControlsManager.activitySelection) { newSelection in
                    DeviceActivityManager.shared.startMonitoring(for: newSelection)
                }
            }
        }
        .padding(16)
        .background(Color(uiColor: .secondarySystemGroupedBackground), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
    }
}

