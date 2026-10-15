import Foundation
import ManagedSettings
import ManagedSettingsUI
import UIKit

/**
 * Shield configuration providing branded Unreel visuals when an app is shielded.
 */
class UnreelShieldConfigurationDataSource: ShieldConfigurationDataSource {
    override func configuration(shielding application: Application) -> ShieldConfiguration {
        return ShieldConfiguration(
            backgroundBlurStyle: .systemUltraThinMaterialDark,
            backgroundColor: UIColor(red: 0.07, green: 0.07, blue: 0.12, alpha: 1.0),
            icon: UIImage(systemName: "shield.slash.fill"),
            title: ShieldConfiguration.Label(
                text: "Scroll Limit Reached",
                color: .white
            ),
            subtitle: ShieldConfiguration.Label(
                text: "You've reached your scheduled limit on Unreel. Take a mindful break and return later.",
                color: UIColor(white: 0.8, alpha: 1.0)
            ),
            primaryButtonLabel: ShieldConfiguration.Label(
                text: "Step Away",
                color: .white
            ),
            primaryButtonBackgroundColor: UIColor(red: 0.39, green: 0.40, blue: 0.95, alpha: 1.0)
        )
    }
}
