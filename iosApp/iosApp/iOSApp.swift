import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        IosAppModuleKt.startKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}