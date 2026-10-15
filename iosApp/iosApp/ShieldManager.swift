import Foundation
import ManagedSettings
import FamilyControls

/**
 * Manager handling system-level application shielding (blocking)
 * via ManagedSettingsStore when limits are reached on iOS.
 */
final class ShieldManager {
    static let shared = ShieldManager()

    private let store = ManagedSettingsStore()

    /**
     * Applies shields to all selected applications and categories.
     */
    func applyShield(for selection: FamilyActivitySelection) {
        store.shield.applications = selection.applicationTokens.isEmpty ? nil : selection.applicationTokens
        store.shield.applicationCategories = selection.categoryTokens.isEmpty
            ? nil
            : ShieldSettings.ActivityCategoryPolicy.specific(selection.categoryTokens)
        store.shield.webDomains = selection.webDomainTokens.isEmpty ? nil : selection.webDomainTokens
    }

    /**
     * Clears all active application shields.
     */
    func clearShield() {
        store.shield.applications = nil
        store.shield.applicationCategories = nil
        store.shield.webDomains = nil
    }
}
