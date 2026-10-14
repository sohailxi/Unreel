import SwiftUI
import Shared

struct ContentView: View {
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "timer")
                .font(.system(size: 48))
                .foregroundStyle(.tint)
            Text("Unreel")
                .font(.title)
                .bold()
            Text("Know your scroll. Reclaim your time.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
