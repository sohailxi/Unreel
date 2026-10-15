package app.unreel.android.detector

import android.view.accessibility.AccessibilityEvent
import app.unreel.model.DataSource
import app.unreel.testing.FakeClock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.datetime.Instant

class ReelDetectorEngineTest {

    private lateinit var fakeClock: FakeClock
    private lateinit var detector: ReelDetectorEngine

    @BeforeTest
    fun setUp() {
        fakeClock = FakeClock(Instant.fromEpochMilliseconds(1_700_000_000_000L))
        detector = ReelDetectorEngine(clock = fakeClock)
    }

    @Test
    fun `unsupported package produces null event`() {
        val event = detector.processEvent(
            packageName = "com.whatsapp",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNull(event)
    }

    @Test
    fun `non-scroll event produces null event even for supported package`() {
        val event = detector.processEvent(
            packageName = "com.instagram.android",
            eventType = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
        )
        assertNull(event)
    }

    @Test
    fun `first scroll for supported app produces valid ReelEvent with default dwell`() {
        val event = detector.processEvent(
            packageName = "com.instagram.android",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        assertNotNull(event)
        assertEquals("com.instagram.android", event.appId)
        assertEquals(DataSource.EXACT, event.source)
        assertEquals(fakeClock.now(), event.tsUtc)
        assertEquals(ReelDetectorEngine.DEFAULT_INITIAL_DWELL_MS, event.dwellMs)
        assertTrue(event.id.startsWith("reel-com.instagram.android-"))
    }

    @Test
    fun `rapid scroll under debounce threshold is ignored`() {
        // First scroll
        val firstEvent = detector.processEvent(
            packageName = "com.google.android.youtube",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(firstEvent)

        // Rapid scroll 200ms later (under 500ms debounce threshold)
        fakeClock.advanceBy(200L)
        val rapidEvent = detector.processEvent(
            packageName = "com.google.android.youtube",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNull(rapidEvent)
    }

    @Test
    fun `subsequent scroll after dwell calculates accurate dwell time`() {
        // First reel
        detector.processEvent(
            packageName = "com.zhiliaoapp.musically", // TikTok
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        // User spends 8 seconds on this reel
        fakeClock.advanceBy(8_000L)

        val secondEvent = detector.processEvent(
            packageName = "com.zhiliaoapp.musically",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        assertNotNull(secondEvent)
        assertEquals("com.zhiliaoapp.musically", secondEvent.appId)
        assertEquals(8_000L, secondEvent.dwellMs)
        assertEquals(fakeClock.now(), secondEvent.tsUtc)
    }

    @Test
    fun `dwell time is capped at maximum session threshold`() {
        detector.processEvent(
            packageName = "com.instagram.android",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        // Advance by 10 minutes (600,000ms) - exceeds max dwell cap of 5 minutes
        fakeClock.advanceBy(600_000L)

        val event = detector.processEvent(
            packageName = "com.instagram.android",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        assertNotNull(event)
        assertEquals(ReelDetectorEngine.MAX_DWELL_MS, event.dwellMs)
    }

    @Test
    fun `switching apps resets previous app dwell timer`() {
        // Scroll on Instagram
        detector.processEvent(
            packageName = "com.instagram.android",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        fakeClock.advanceBy(5_000L)

        // Switch and scroll on YouTube
        val ytEvent = detector.processEvent(
            packageName = "com.google.android.youtube",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )

        assertNotNull(ytEvent)
        assertEquals("com.google.android.youtube", ytEvent.appId)
        // Since it's the first scroll on YouTube, it gets initial default dwell
        assertEquals(ReelDetectorEngine.DEFAULT_INITIAL_DWELL_MS, ytEvent.dwellMs)
    }

    @Test
    fun `supported packages include all target apps`() {
        val supported = ReelDetectorEngine.SUPPORTED_PACKAGES
        assertTrue(supported.contains("com.instagram.android"))
        assertTrue(supported.contains("com.google.android.youtube"))
        assertTrue(supported.contains("com.zhiliaoapp.musically"))
        assertTrue(supported.contains("com.ss.android.ugc.trill"))
        assertTrue(supported.contains("com.snapchat.android"))
        assertTrue(supported.contains("com.facebook.katana"))
    }
}
