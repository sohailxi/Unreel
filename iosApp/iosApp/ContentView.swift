import SwiftUI
import Shared

struct ContentView: View {
    @StateObject private var homeVM = HomeObservable()
    @StateObject private var statsVM = StatsObservable()
    @StateObject private var rulesVM = RulesObservable()

    var body: some View {
        TabView {
            HomeView(viewModel: homeVM)
                .tabItem {
                    Label("Scroll", systemImage: "play.rectangle.on.rectangle")
                }

            StatsView(viewModel: statsVM)
                .tabItem {
                    Label("Trends", systemImage: "chart.line.uptrend.xyaxis")
                }

            RulesView(viewModel: rulesVM)
                .tabItem {
                    Label("Limits", systemImage: "shield.lefthalf.filled")
                }
        }
        .tint(Color.accentColor)
    }
}

#Preview {
    ContentView()
}
