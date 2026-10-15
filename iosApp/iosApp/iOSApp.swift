import SwiftUI
import Shared
import FamilyControls

@main
struct iOSApp: App {
    @StateObject private var activityManager = ActivityKitManager.shared
    @StateObject private var familyControlsManager = FamilyControlsManager.shared
    private var unsubscribeLiveSurface: (() -> Void)?

    init() {
        IosAppModuleKt.startKoinIos()
        #if DEBUG
        _ = SnapshotReaderTests.runAllTests()
        #endif
        let helper = KoinHelper()
        self.unsubscribeLiveSurface = helper.observeLiveSurface { surfaceState in
            Task { @MainActor in
                ActivityKitManager.shared.syncWithSurfaceState(surfaceState)
                let isLimitReached = (surfaceState.limitProgress?.floatValue ?? 0) >= 1.0
                if isLimitReached {
                    ShieldManager.shared.applyShield(for: FamilyControlsManager.shared.activitySelection)
                } else {
                    ShieldManager.shared.clearShield()
                }
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(activityManager)
                .environmentObject(familyControlsManager)
        }
    }
}