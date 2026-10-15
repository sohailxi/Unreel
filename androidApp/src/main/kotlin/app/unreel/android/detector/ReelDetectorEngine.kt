package app.unreel.android.detector

import android.view.accessibility.AccessibilityEvent
import app.unreel.model.DataSource
import app.unreel.model.ReelEvent
import app.unreel.time.AppClock
import co.touchlab.kermit.Logger
import kotlinx.datetime.Instant

/**
 * Core reel detection engine on Android.
 *
 * Evaluates accessibility events from allowed short-video apps, computes dwell times,
 * filters rapid debounce events, and generates [ReelEvent]s.
 *
 * Privacy rules:
 * - Only inspects package name, event type, and timestamps.
 * - Never reads, logs, or stores on-screen text or user content.
 */
class ReelDetectorEngine(
    private val clock: AppClock,
    private val debounceThresholdMs: Long = DEFAULT_DEBOUNCE_THRESHOLD_MS,
    private val logger: Logger = Logger.withTag("ReelDetectorEngine"),
) {
    private var lastEventPackage: String? = null
    private var lastEventTimestamp: Instant? = null

    fun processEvent(
        packageName: String,
        eventType: Int,
    ): ReelEvent? {
        if (!SUPPORTED_PACKAGES.contains(packageName)) {
            return null
        }

        if (eventType != AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            return null
        }

        val now = clock.now()
        val currentMs = now.toEpochMilliseconds()

        val lastPackage = lastEventPackage
        val lastTimestamp = lastEventTimestamp

        // If the same package was scrolled recently, check debounce
        if (lastPackage == packageName && lastTimestamp != null) {
            val elapsedMs = currentMs - lastTimestamp.toEpochMilliseconds()
            if (elapsedMs < debounceThresholdMs) {
                logger.d { "Debounced scroll event on $packageName ($elapsedMs ms < $debounceThresholdMs ms)" }
                return null
            }

            val dwellMs = elapsedMs.coerceAtMost(MAX_DWELL_MS)
            lastEventTimestamp = now

            return ReelEvent(
                id = "reel-$packageName-$currentMs",
                appId = packageName,
                tsUtc = now,
                dwellMs = dwellMs,
                source = DataSource.EXACT,
            )
        }

        // New session or switched package
        lastEventPackage = packageName
        lastEventTimestamp = now

        return ReelEvent(
            id = "reel-$packageName-$currentMs",
            appId = packageName,
            tsUtc = now,
            dwellMs = DEFAULT_INITIAL_DWELL_MS,
            source = DataSource.EXACT,
        )
    }

    companion object {
        const val DEFAULT_DEBOUNCE_THRESHOLD_MS = 500L
        const val DEFAULT_INITIAL_DWELL_MS = 2_000L
        const val MAX_DWELL_MS = 300_000L // 5 minutes max dwell per reel

        val SUPPORTED_PACKAGES: Set<String> = app.unreel.model.TrackedAppCatalog.allAndroidPackages
    }
}
