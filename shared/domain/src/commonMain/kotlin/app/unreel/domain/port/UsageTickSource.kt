package app.unreel.domain.port

import app.unreel.model.UsageTick
import kotlinx.coroutines.flow.Flow

/**
 * Port for receiving iOS usage ticks from DeviceActivityMonitor.
 * No-op on Android.
 */
interface UsageTickSource {
    val ticks: Flow<UsageTick>
}
