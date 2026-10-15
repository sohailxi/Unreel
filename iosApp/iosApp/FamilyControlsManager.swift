import Foundation
import FamilyControls
import SwiftUI

/**
 * Manager responsible for Screen Time / FamilyControls authorization
 * and persisting user-selected applications (FamilyActivitySelection).
 */
@MainActor
final class FamilyControlsManager: ObservableObject {
    static let shared = FamilyControlsManager()

    @Published private(set) var isAuthorized: Bool = false
    @Published var activitySelection: FamilyActivitySelection = FamilyActivitySelection() {
        didSet {
            saveActivitySelection()
        }
    }

    private let userDefaults = UserDefaults(suiteName: SnapshotReader.appGroupId)
    private let selectionKey = "unreel_family_activity_selection"

    init() {
        loadActivitySelection()
        checkAuthorizationStatus()
    }

    func requestAuthorization() async {
        do {
            try await AuthorizationCenter.shared.requestAuthorization(for: .individual)
            checkAuthorizationStatus()
        } catch {
            // Fails on simulator or unentitled environments
            checkAuthorizationStatus()
        }
    }

    func checkAuthorizationStatus() {
        let status = AuthorizationCenter.shared.authorizationStatus
        self.isAuthorized = (status == .approved)
    }

    private func saveActivitySelection() {
        guard let defaults = userDefaults else { return }
        do {
            let data = try PropertyListEncoder().encode(activitySelection)
            defaults.set(data, forKey: selectionKey)
        } catch {
            // Encoding failed
        }
    }

    private func loadActivitySelection() {
        guard let defaults = userDefaults,
              let data = defaults.data(forKey: selectionKey) else { return }
        do {
            self.activitySelection = try PropertyListDecoder().decode(FamilyActivitySelection.self, from: data)
        } catch {
            // Decoding failed
        }
    }
}
