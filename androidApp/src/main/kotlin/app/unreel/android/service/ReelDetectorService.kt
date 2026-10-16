package app.unreel.android.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import app.unreel.android.detector.ReelDetectorEngine
import app.unreel.android.platform.AndroidScrollEventSource
import app.unreel.domain.usecase.IngestReelEventUseCase
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * AccessibilityService for reel detection.
 *
 * Privacy constraints (non-negotiable):
 * - Restricted to tracked app allowlist via android:packageNames
 * - Never reads, logs or stores node text, messages or user content
 * - Stores only counters, timestamps and package names
 * - No raw accessibility data leaves the device
 */
class ReelDetectorService : AccessibilityService(), KoinComponent {

    private val logger = Logger.withTag("ReelDetectorService")
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val detectorEngine: ReelDetectorEngine by inject()
    private val scrollEventSource: AndroidScrollEventSource by inject()
    private val ingestReelEventUseCase: IngestReelEventUseCase by inject()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        val reelEvent = detectorEngine.processEvent(
            packageName = packageName,
            eventType = event.eventType,
        ) ?: return

        logger.i { "Detected reel event: app=${reelEvent.appId} (pkg=$packageName), dwell=${reelEvent.dwellMs}ms" }
        scrollEventSource.emit(reelEvent)
        serviceScope.launch {
            try {
                ingestReelEventUseCase(reelEvent)
            } catch (t: Throwable) {
                logger.e(t) { "Error ingesting reel event for $packageName" }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        try {
            val info = serviceInfo ?: android.accessibilityservice.AccessibilityServiceInfo()
            val packages = app.unreel.model.TrackedAppCatalog.allAndroidPackages.toTypedArray()
            info.packageNames = packages
            info.flags = info.flags or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            serviceInfo = info
            logger.i { "ReelDetectorService connected with ${packages.size} packages" }
        } catch (t: Throwable) {
            logger.w(t) { "Could not dynamically update AccessibilityServiceInfo packageNames" }
        }
        _isConnected.value = true
    }

    override fun onInterrupt() {
        logger.w { "ReelDetectorService interrupted" }
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        _isConnected.value = false
        logger.i { "ReelDetectorService unbound" }
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        _isConnected.value = false
        serviceScope.cancel()
    }

    companion object {
        private val _isConnected = kotlinx.coroutines.flow.MutableStateFlow(false)
        val isConnected: kotlinx.coroutines.flow.StateFlow<Boolean> = _isConnected
    }
}
