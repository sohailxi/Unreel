import SwiftUI
import Shared

@main
struct iOSApp: App {
    @StateObject private var activityManager = ActivityKitManager.shared
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
            }
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(activityManager)
        }
    }
}