import Foundation
import ActivityKit
import WidgetKit
import SwiftUI
import Shared

/**
 * Manager handling ActivityKit Live Activity lifecycles (Dynamic Island & Lock Screen)
 * and triggering WidgetKit timeline refreshes when KMP surface state updates.
 */
@MainActor
final class ActivityKitManager: ObservableObject {
    static let shared = ActivityKitManager()

    @Published private(set) var isLiveActivityActive: Bool = false
    @Published private(set) var activeActivityId: String? = nil

    private var currentActivity: Activity<UnreelActivityAttributes>?

    init() {
        // Find any existing running activity on startup
        if let existing = Activity<UnreelActivityAttributes>.activities.first {
            self.currentActivity = existing
            self.isLiveActivityActive = true
            self.activeActivityId = existing.id
        }
    }

    /**
     * Starts a Live Activity for the specified tracked app.
     */
    func startLiveActivity(
        appName: String = "Instagram Reels",
        sessionReels: Int = 0,
        todayReels: Int = 0,
        sessionDurationMinutes: Int = 0,
        limitProgress: Float? = nil
    ) {
        guard ActivityAuthorizationInfo().areActivitiesEnabled else {
            return
        }

        // End any existing session activity first
        if let existing = currentActivity {
            Task {
                await existing.end(nil, dismissalPolicy: .immediate)
            }
        }

        let attributes = UnreelActivityAttributes(appName: appName)
        let initialContentState = UnreelActivityAttributes.ContentState(
            sessionReels: sessionReels,
            todayReels: todayReels,
            sessionDurationMinutes: sessionDurationMinutes,
            limitProgress: limitProgress,
            isNearingLimit: (limitProgress ?? 0) >= 0.85,
            isBlocked: false,
            source: "ESTIMATED"
        )

        do {
            let activity = try Activity.request(
                attributes: attributes,
                content: .init(state: initialContentState, staleDate: nil),
                pushType: nil
            )
            self.currentActivity = activity
            self.isLiveActivityActive = true
            self.activeActivityId = activity.id
        } catch {
            // Activity request failed or simulator lacks support
        }
    }

    /**
     * Updates content state for the active Live Activity.
     */
    func updateLiveActivity(
        sessionReels: Int,
        todayReels: Int,
        sessionDurationMinutes: Int,
        limitProgress: Float?,
        isBlocked: Bool = false
    ) {
        guard let activity = currentActivity else { return }

        let updatedContent = UnreelActivityAttributes.ContentState(
            sessionReels: sessionReels,
            todayReels: todayReels,
            sessionDurationMinutes: sessionDurationMinutes,
            limitProgress: limitProgress,
            isNearingLimit: (limitProgress ?? 0) >= 0.85,
            isBlocked: isBlocked,
            source: "ESTIMATED"
        )

        Task {
            await activity.update(
                ActivityContent(state: updatedContent, staleDate: nil)
            )
        }
    }

    /**
     * Ends the active Live Activity.
     */
    func endLiveActivity() {
        guard let activity = currentActivity else { return }
        Task {
            await activity.end(nil, dismissalPolicy: .immediate)
            self.currentActivity = nil
            self.isLiveActivityActive = false
            self.activeActivityId = nil
        }
    }

    /**
     * Syncs native iOS surfaces with shared KMP [IosSurfaceState].
     */
    func syncWithSurfaceState(_ state: IosSurfaceState) {
        let sessionDurationMinutes = Int(max(0, state.todayDurationMs / 60_000))

        if state.isSessionActive {
            if currentActivity == nil {
                startLiveActivity(
                    appName: "Reels",
                    sessionReels: Int(state.sessionReels),
                    todayReels: Int(state.todayReels),
                    sessionDurationMinutes: sessionDurationMinutes,
                    limitProgress: state.limitProgress?.floatValue
                )
            } else {
                updateLiveActivity(
                    sessionReels: Int(state.sessionReels),
                    todayReels: Int(state.todayReels),
                    sessionDurationMinutes: sessionDurationMinutes,
                    limitProgress: state.limitProgress?.floatValue,
                    isBlocked: false
                )
            }
        } else {
            if currentActivity != nil {
                endLiveActivity()
            }
        }

        // Reload WidgetKit timelines to display latest snapshot
        reloadWidgets()
    }

    /**
     * Notifies WidgetKit to reload all timelines.
     */
    func reloadWidgets() {
        WidgetCenter.shared.reloadAllTimelines()
    }
}
