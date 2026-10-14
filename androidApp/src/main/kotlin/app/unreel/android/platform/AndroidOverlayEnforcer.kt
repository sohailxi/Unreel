package app.unreel.android.platform

import app.unreel.domain.port.Enforcer
import app.unreel.model.Decision
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of [Enforcer].
 * Manages active enforcement decisions (Block, Friction, Nudge) and exposes
 * observable state for overlay UI and blocking mechanisms.
 */
class AndroidOverlayEnforcer : Enforcer {
    private val _activeDecision = MutableStateFlow<Decision>(Decision.Allow)
    val activeDecision: StateFlow<Decision> = _activeDecision.asStateFlow()

    private val _isEnforcing = MutableStateFlow(false)
    val isEnforcing: StateFlow<Boolean> = _isEnforcing.asStateFlow()

    override suspend fun enforce(decision: Decision) {
        _activeDecision.value = decision
        _isEnforcing.value = decision !is Decision.Allow
    }

    override suspend fun clearEnforcement() {
        _activeDecision.value = Decision.Allow
        _isEnforcing.value = false
    }
}
