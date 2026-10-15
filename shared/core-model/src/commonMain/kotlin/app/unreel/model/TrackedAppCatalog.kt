package app.unreel.model

/**
 * Universal catalog of supported short video and infinite-scrolling platforms.
 *
 * Covers dedicated reel formats (Instagram Reels, YouTube Shorts, TikTok, Snapchat Spotlight, Facebook Reels, Likee, Kwai, Triller)
 * as well as endless social scrolling feeds (Reddit, X/Twitter, Threads, Pinterest, Bluesky, Twitch Clips, Tumblr, Imgur).
 */
object TrackedAppCatalog {
    const val CATEGORY_SHORT_VIDEO = "Short Video"
    const val CATEGORY_FEED_SCROLLING = "Feed Scrolling"

    val defaultApps: List<TrackedApp> = listOf(
        // Short Video / Reels Platforms
        TrackedApp(
            id = "instagram",
            platform = Platform.ANDROID,
            displayName = "Instagram Reels",
            packageOrTokenRef = "com.instagram.android",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "instagram_lite",
            platform = Platform.ANDROID,
            displayName = "Instagram Reels (Lite)",
            packageOrTokenRef = "com.instagram.lite",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "youtube",
            platform = Platform.ANDROID,
            displayName = "YouTube Shorts",
            packageOrTokenRef = "com.google.android.youtube",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "tiktok",
            platform = Platform.ANDROID,
            displayName = "TikTok",
            packageOrTokenRef = "com.zhiliaoapp.musically",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "tiktok_trill",
            platform = Platform.ANDROID,
            displayName = "TikTok",
            packageOrTokenRef = "com.ss.android.ugc.trill",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "tiktok_lite",
            platform = Platform.ANDROID,
            displayName = "TikTok Lite",
            packageOrTokenRef = "com.zhiliaoapp.musically.go",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "snapchat",
            platform = Platform.ANDROID,
            displayName = "Snapchat Spotlight",
            packageOrTokenRef = "com.snapchat.android",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "facebook",
            platform = Platform.ANDROID,
            displayName = "Facebook Reels",
            packageOrTokenRef = "com.facebook.katana",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "facebook_lite",
            platform = Platform.ANDROID,
            displayName = "Facebook Reels (Lite)",
            packageOrTokenRef = "com.facebook.lite",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "likee",
            platform = Platform.ANDROID,
            displayName = "Likee",
            packageOrTokenRef = "video.like",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "kwai",
            platform = Platform.ANDROID,
            displayName = "Kwai",
            packageOrTokenRef = "com.kwai.video",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "triller",
            platform = Platform.ANDROID,
            displayName = "Triller",
            packageOrTokenRef = "co.triller.droid",
            category = CATEGORY_SHORT_VIDEO,
        ),

        // Social & Infinite-Feed Scrolling Platforms
        TrackedApp(
            id = "reddit",
            platform = Platform.ANDROID,
            displayName = "Reddit",
            packageOrTokenRef = "com.reddit.frontpage",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "twitter",
            platform = Platform.ANDROID,
            displayName = "X (Twitter)",
            packageOrTokenRef = "com.twitter.android",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "twitter_lite",
            platform = Platform.ANDROID,
            displayName = "X (Twitter Lite)",
            packageOrTokenRef = "com.twitter.android.lite",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "threads",
            platform = Platform.ANDROID,
            displayName = "Threads",
            packageOrTokenRef = "com.instagram.barcelona",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "pinterest",
            platform = Platform.ANDROID,
            displayName = "Pinterest",
            packageOrTokenRef = "com.pinterest",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "bluesky",
            platform = Platform.ANDROID,
            displayName = "Bluesky",
            packageOrTokenRef = "xyz.blueskyweb.app",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "twitch",
            platform = Platform.ANDROID,
            displayName = "Twitch Clips",
            packageOrTokenRef = "tv.twitch.android.app",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "tumblr",
            platform = Platform.ANDROID,
            displayName = "Tumblr",
            packageOrTokenRef = "com.tumblr",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "imgur",
            platform = Platform.ANDROID,
            displayName = "Imgur",
            packageOrTokenRef = "com.imgur.mobile",
            category = CATEGORY_FEED_SCROLLING,
        ),
    )

    val allAndroidPackages: Set<String> = defaultApps.map { it.packageOrTokenRef }.toSet()

    private val packageToAppMap: Map<String, TrackedApp> = defaultApps.associateBy { it.packageOrTokenRef }
    private val idToAppMap: Map<String, TrackedApp> = defaultApps.associateBy { it.id }

    fun getAppsByCategory(category: String): List<TrackedApp> =
        defaultApps.filter { it.category == category }

    fun isSupported(packageOrId: String): Boolean =
        packageToAppMap.containsKey(packageOrId) || idToAppMap.containsKey(packageOrId)

    fun getDisplayName(packageOrId: String): String {
        packageToAppMap[packageOrId]?.let { return it.displayName }
        idToAppMap[packageOrId]?.let { return it.displayName }
        val parts = packageOrId.split(".")
        val candidate = parts.lastOrNull { it != "android" && it != "app" && it != "mobile" } ?: parts.lastOrNull()
        return candidate?.replaceFirstChar { it.uppercase() } ?: packageOrId
    }

    fun getCategory(packageOrId: String): String =
        packageToAppMap[packageOrId]?.category ?: idToAppMap[packageOrId]?.category ?: CATEGORY_SHORT_VIDEO
}
