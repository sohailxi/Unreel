package app.unreel.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrackedAppCatalogTest {

    @Test
    fun `defaultCatalog contains both short video and scrolling platforms`() {
        val apps = TrackedAppCatalog.defaultApps
        assertTrue(apps.size >= 16, "Expected at least 16 short video & scrolling platforms")

        val categories = apps.map { it.category }.toSet()
        assertTrue(categories.contains(TrackedAppCatalog.CATEGORY_SHORT_VIDEO))
        assertTrue(categories.contains(TrackedAppCatalog.CATEGORY_FEED_SCROLLING))
    }

    @Test
    fun `short video platforms include Instagram YouTube Shorts TikTok Snapchat Facebook and emerging reel apps`() {
        val shortVideoApps = TrackedAppCatalog.getAppsByCategory(TrackedAppCatalog.CATEGORY_SHORT_VIDEO)
        val names = shortVideoApps.map { it.displayName }

        assertTrue(names.contains("Instagram Reels"))
        assertTrue(names.contains("YouTube Shorts"))
        assertTrue(names.contains("TikTok"))
        assertTrue(names.contains("Snapchat Spotlight"))
        assertTrue(names.contains("Facebook Reels"))
        assertTrue(names.contains("Likee"))
        assertTrue(names.contains("Kwai"))
        assertTrue(names.contains("Triller"))
    }

    @Test
    fun `scrolling platforms include Reddit X Twitter Threads Pinterest and Bluesky`() {
        val scrollingApps = TrackedAppCatalog.getAppsByCategory(TrackedAppCatalog.CATEGORY_FEED_SCROLLING)
        val names = scrollingApps.map { it.displayName }

        assertTrue(names.contains("Reddit"))
        assertTrue(names.contains("X (Twitter)"))
        assertTrue(names.contains("Threads"))
        assertTrue(names.contains("Pinterest"))
        assertTrue(names.contains("Bluesky"))
        assertTrue(names.contains("Twitch Clips"))
    }

    @Test
    fun `getDisplayName maps known packages to human friendly names and falls back gracefully`() {
        assertEquals("Instagram Reels", TrackedAppCatalog.getDisplayName("com.instagram.android"))
        assertEquals("YouTube Shorts", TrackedAppCatalog.getDisplayName("com.google.android.youtube"))
        assertEquals("TikTok", TrackedAppCatalog.getDisplayName("com.zhiliaoapp.musically"))
        assertEquals("TikTok", TrackedAppCatalog.getDisplayName("com.ss.android.ugc.trill"))
        assertEquals("Reddit", TrackedAppCatalog.getDisplayName("com.reddit.frontpage"))
        assertEquals("X (Twitter)", TrackedAppCatalog.getDisplayName("com.twitter.android"))
        assertEquals("Snapchat Spotlight", TrackedAppCatalog.getDisplayName("com.snapchat.android"))

        // Graceful fallback for unknown app IDs
        assertEquals("Custom", TrackedAppCatalog.getDisplayName("custom.app"))
        assertEquals("Unknown Feed", TrackedAppCatalog.getDisplayName("Unknown Feed"))
    }

    @Test
    fun `isSupported returns true for all short video and scrolling packages`() {
        assertTrue(TrackedAppCatalog.isSupported("com.instagram.android"))
        assertTrue(TrackedAppCatalog.isSupported("com.google.android.youtube"))
        assertTrue(TrackedAppCatalog.isSupported("com.zhiliaoapp.musically"))
        assertTrue(TrackedAppCatalog.isSupported("com.reddit.frontpage"))
        assertTrue(TrackedAppCatalog.isSupported("com.twitter.android"))
        assertTrue(TrackedAppCatalog.isSupported("com.pinterest"))
        assertTrue(TrackedAppCatalog.isSupported("com.instagram.barcelona"))
        assertTrue(TrackedAppCatalog.isSupported("video.like"))
        assertTrue(TrackedAppCatalog.isSupported("tv.twitch.android.app"))

        assertFalse(TrackedAppCatalog.isSupported("com.android.calculator2"))
        assertFalse(TrackedAppCatalog.isSupported("com.google.android.calendar"))
    }

    @Test
    fun `allAndroidPackages exposes set of all supported packages`() {
        val packages = TrackedAppCatalog.allAndroidPackages
        assertTrue(packages.contains("com.instagram.android"))
        assertTrue(packages.contains("com.google.android.youtube"))
        assertTrue(packages.contains("com.reddit.frontpage"))
        assertTrue(packages.contains("com.twitter.android"))
        assertTrue(packages.size >= 16)
    }
}
