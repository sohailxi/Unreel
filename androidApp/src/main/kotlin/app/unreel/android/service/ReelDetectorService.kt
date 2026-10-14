package app.unreel.android.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import app.unreel.android.platform.AndroidScrollEventSource
import app.unreel.domain.usecase.IngestReelEventUseCase
import app.unreel.model.DataSource
import app.unreel.model.ReelEvent
import app.unreel.time.AppClock
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

    private val clock: AppClock by inject()
    private val scrollEventSource: AndroidScrollEventSource by inject()
    private val ingestReelEventUseCase: IngestReelEventUseCase by inject()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val packageName = event.packageName?.toString() ?: return

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val now = clock.now()
            val reelEvent = ReelEvent(
                id = "reel-${packageName}-${now.toEpochMilliseconds()}",
                appId = packageName,
                tsUtc = now,
                dwellMs = 1500L,
                source = DataSource.EXACT,
            )
            scrollEventSource.emit(reelEvent)
            serviceScope.launch {
                try {
                    ingestReelEventUseCase(reelEvent)
                } catch (t: Throwable) {
                    logger.e(t) { "Error ingesting reel event for $packageName" }
                }
            }
        }
    }

    override fun onInterrupt() {
        logger.w { "ReelDetectorService interrupted" }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
