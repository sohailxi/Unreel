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
        assertTrue(supported.contains("com.reddit.frontpage"))
        assertTrue(supported.contains("com.twitter.android"))
        assertTrue(supported.contains("com.pinterest"))
        assertTrue(supported.contains("com.instagram.barcelona"))
        assertTrue(supported.contains("video.like"))
    }

    @Test
    fun `short video and scrolling platforms like Reddit and Twitter produce valid scroll events`() {
        val redditEvent = detector.processEvent(
            packageName = "com.reddit.frontpage",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(redditEvent)
        assertEquals("com.reddit.frontpage", redditEvent.appId)

        fakeClock.advanceBy(4_000L)
        val twitterEvent = detector.processEvent(
            packageName = "com.twitter.android",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(twitterEvent)
        assertEquals("com.twitter.android", twitterEvent.appId)
    }

    @Test
    fun `page snap event TYPE_VIEW_SELECTED produces valid ReelEvent for short video pager transitions`() {
        val snapEvent = detector.processEvent(
            packageName = "com.instagram.android",
            eventType = AccessibilityEvent.TYPE_VIEW_SELECTED,
        )
        assertNotNull(snapEvent)
        assertEquals("com.instagram.android", snapEvent.appId)
        assertEquals(DataSource.EXACT, snapEvent.source)
    }

    @Test
    fun `consecutive scroll and page snap events during single reel swipe are debounced to single event`() {
        val firstEvent = detector.processEvent(
            packageName = "com.zhiliaoapp.musically",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(firstEvent)

        // Pager snap event fires 100ms later during same swipe gesture
        fakeClock.advanceBy(100L)
        val snapEvent = detector.processEvent(
            packageName = "com.zhiliaoapp.musically",
            eventType = AccessibilityEvent.TYPE_VIEW_SELECTED,
        )
        assertNull(snapEvent, "Second event during same swipe should be debounced")
    }

    @Test
    fun `global short video apps like Moj Josh SnackVideo and Lemon8 produce valid reel events`() {
        val mojEvent = detector.processEvent(
            packageName = "in.mohalla.video",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(mojEvent)
        assertEquals("in.mohalla.video", mojEvent.appId)

        fakeClock.advanceBy(3_000L)
        val joshEvent = detector.processEvent(
            packageName = "com.eterno.shortvideos",
            eventType = AccessibilityEvent.TYPE_VIEW_SELECTED,
        )
        assertNotNull(joshEvent)
        assertEquals("com.eterno.shortvideos", joshEvent.appId)

        fakeClock.advanceBy(3_000L)
        val lemon8Event = detector.processEvent(
            packageName = "com.bd.nproject",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(lemon8Event)
        assertEquals("com.bd.nproject", lemon8Event.appId)
    }

    @Test
    fun `alternative and modded YouTube clients like Morphe and ReVanced map to canonical YouTube Shorts appId`() {
        val morpheEvent = detector.processEvent(
            packageName = "app.morphe.android.youtube",
            eventType = AccessibilityEvent.TYPE_VIEW_SCROLLED,
        )
        assertNotNull(morpheEvent)
        assertEquals("com.google.android.youtube", morpheEvent.appId)
        assertEquals(DataSource.EXACT, morpheEvent.source)

        fakeClock.advanceBy(4_000L)
        val revancedEvent = detector.processEvent(
            packageName = "app.revanced.android.youtube",
            eventType = AccessibilityEvent.TYPE_VIEW_SELECTED,
        )
        assertNotNull(revancedEvent)
        assertEquals("com.google.android.youtube", revancedEvent.appId)
    }
}

