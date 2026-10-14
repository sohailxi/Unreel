package app.unreel.domain.port

import app.unreel.model.Decision

/**
 * Port for enforcing limit decisions on the platform.
 * Android: show block screen, overlay, or reels-only block.
 * iOS: ManagedSettingsStore shields, DeviceActivitySchedule.
 */
interface Enforcer {
    suspend fun enforce(decision: Decision)
    suspend fun clearEnforcement()
}
