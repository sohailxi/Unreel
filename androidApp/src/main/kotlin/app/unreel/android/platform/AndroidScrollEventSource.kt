package app.unreel.android.platform

import app.unreel.domain.port.ScrollEventSource
import app.unreel.model.ReelEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Android implementation of [ScrollEventSource].
 * Dispatches raw [ReelEvent] streams captured from accessibility scroll events.
 */
class AndroidScrollEventSource : ScrollEventSource {

    private val _events = MutableSharedFlow<ReelEvent>(extraBufferCapacity = 64)
    override val events: Flow<ReelEvent> = _events.asSharedFlow()

    fun emit(event: ReelEvent) {
        _events.tryEmit(event)
    }
}
