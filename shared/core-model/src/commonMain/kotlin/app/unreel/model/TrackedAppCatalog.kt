package app.unreel.model

/**
 * Universal catalog of supported short video and infinite-scrolling platforms.
 *
 * Covers all types of short video reels (Instagram Reels, YouTube Shorts, TikTok, Snapchat Spotlight,
 * Facebook Reels, Likee, Kwai, SnackVideo, Moj, Josh, Triller, Chingari, Rizzle, Hipi, Clapper, Lemon8, Vskit)
 * as well as endless social scrolling feeds (Reddit, X/Twitter, Threads, Pinterest, Bluesky, Twitch Clips,
 * Tumblr, Imgur, Mastodon, ShareChat) on both Android and iOS.
 */
object TrackedAppCatalog {
    const val CATEGORY_SHORT_VIDEO = "Short Video"
    const val CATEGORY_FEED_SCROLLING = "Feed Scrolling"

    val defaultApps: List<TrackedApp> = listOf(
        // ==========================================
        // Android Short Video & Reels Platforms
        // ==========================================
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
            id = "youtube_kids",
            platform = Platform.ANDROID,
            displayName = "YouTube Kids Shorts",
            packageOrTokenRef = "com.google.android.apps.youtube.kids",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "youtube_creator",
            platform = Platform.ANDROID,
            displayName = "YouTube Studio Shorts",
            packageOrTokenRef = "com.google.android.apps.youtube.creator",
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
            id = "douyin",
            platform = Platform.ANDROID,
            displayName = "Douyin",
            packageOrTokenRef = "com.ss.android.ugc.aweme",
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
            id = "likee_lite",
            platform = Platform.ANDROID,
            displayName = "Likee Lite",
            packageOrTokenRef = "video.like.lite",
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
            id = "kwai_lite",
            platform = Platform.ANDROID,
            displayName = "Kwai Lite",
            packageOrTokenRef = "com.kwai.video.lite",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "snackvideo",
            platform = Platform.ANDROID,
            displayName = "SnackVideo",
            packageOrTokenRef = "com.kwai.bulldog",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "kuaishou",
            platform = Platform.ANDROID,
            displayName = "Kuaishou",
            packageOrTokenRef = "com.smile.gifmaker",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "moj",
            platform = Platform.ANDROID,
            displayName = "Moj",
            packageOrTokenRef = "in.mohalla.video",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "moj_lite",
            platform = Platform.ANDROID,
            displayName = "Moj Lite",
            packageOrTokenRef = "in.mohalla.video.lite",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "josh",
            platform = Platform.ANDROID,
            displayName = "Josh",
            packageOrTokenRef = "com.eterno.shortvideos",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "josh_lite",
            platform = Platform.ANDROID,
            displayName = "Josh Lite",
            packageOrTokenRef = "com.eterno.shortvideos.lite",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "triller",
            platform = Platform.ANDROID,
            displayName = "Triller",
            packageOrTokenRef = "co.triller.droid",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "chingari",
            platform = Platform.ANDROID,
            displayName = "Chingari",
            packageOrTokenRef = "io.chingari.app",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "rizzle",
            platform = Platform.ANDROID,
            displayName = "Rizzle",
            packageOrTokenRef = "com.rizzle.android",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "hipi",
            platform = Platform.ANDROID,
            displayName = "Hipi",
            packageOrTokenRef = "com.zee5.hipi",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "clapper",
            platform = Platform.ANDROID,
            displayName = "Clapper",
            packageOrTokenRef = "com.clapperapp.clapper",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "lemon8",
            platform = Platform.ANDROID,
            displayName = "Lemon8",
            packageOrTokenRef = "com.bd.nproject",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "vskit",
            platform = Platform.ANDROID,
            displayName = "Vskit",
            packageOrTokenRef = "com.infinix.vskit",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "zili",
            platform = Platform.ANDROID,
            displayName = "Zili",
            packageOrTokenRef = "com.star.video",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "dailymotion",
            platform = Platform.ANDROID,
            displayName = "Dailymotion",
            packageOrTokenRef = "com.dailymotion.dailymotion",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "tango",
            platform = Platform.ANDROID,
            displayName = "Tango",
            packageOrTokenRef = "com.sgiggle.production",
            category = CATEGORY_SHORT_VIDEO,
        ),

        // ==========================================
        // Android Social & Infinite-Feed Platforms
        // ==========================================
        TrackedApp(
            id = "reddit",
            platform = Platform.ANDROID,
            displayName = "Reddit",
            packageOrTokenRef = "com.reddit.frontpage",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "reddit_infinity",
            platform = Platform.ANDROID,
            displayName = "Reddit (Infinity)",
            packageOrTokenRef = "com.rubenmayayo.reddit",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "reddit_slide",
            platform = Platform.ANDROID,
            displayName = "Reddit (Slide)",
            packageOrTokenRef = "me.ccrama.redditslide",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "reddit_sync",
            platform = Platform.ANDROID,
            displayName = "Reddit (Sync)",
            packageOrTokenRef = "com.laurencedawson.reddit_sync",
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
        TrackedApp(
            id = "sharechat",
            platform = Platform.ANDROID,
            displayName = "ShareChat",
            packageOrTokenRef = "in.mohalla.sharechat",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "mastodon",
            platform = Platform.ANDROID,
            displayName = "Mastodon",
            packageOrTokenRef = "org.joinmastodon.android",
            category = CATEGORY_FEED_SCROLLING,
        ),

        // ==========================================
        // iOS Short Video & Scrolling Platforms
        // ==========================================
        TrackedApp(
            id = "ios_instagram",
            platform = Platform.IOS,
            displayName = "Instagram Reels",
            packageOrTokenRef = "com.burbn.instagram",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_youtube",
            platform = Platform.IOS,
            displayName = "YouTube Shorts",
            packageOrTokenRef = "com.google.ios.youtube",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_tiktok",
            platform = Platform.IOS,
            displayName = "TikTok",
            packageOrTokenRef = "com.zhiliaoapp.musically",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_snapchat",
            platform = Platform.IOS,
            displayName = "Snapchat Spotlight",
            packageOrTokenRef = "com.toyopagroup.picaboo",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_facebook",
            platform = Platform.IOS,
            displayName = "Facebook Reels",
            packageOrTokenRef = "com.facebook.Facebook",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_likee",
            platform = Platform.IOS,
            displayName = "Likee",
            packageOrTokenRef = "video.like",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_kwai",
            platform = Platform.IOS,
            displayName = "Kwai",
            packageOrTokenRef = "com.kwai.video",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_triller",
            platform = Platform.IOS,
            displayName = "Triller",
            packageOrTokenRef = "co.triller.droid",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_moj",
            platform = Platform.IOS,
            displayName = "Moj",
            packageOrTokenRef = "in.mohalla.video",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_josh",
            platform = Platform.IOS,
            displayName = "Josh",
            packageOrTokenRef = "com.eterno.shortvideos",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_clapper",
            platform = Platform.IOS,
            displayName = "Clapper",
            packageOrTokenRef = "com.clapperapp.clapper",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_lemon8",
            platform = Platform.IOS,
            displayName = "Lemon8",
            packageOrTokenRef = "com.bd.nproject",
            category = CATEGORY_SHORT_VIDEO,
        ),
        TrackedApp(
            id = "ios_reddit",
            platform = Platform.IOS,
            displayName = "Reddit",
            packageOrTokenRef = "com.reddit.Reddit",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_twitter",
            platform = Platform.IOS,
            displayName = "X (Twitter)",
            packageOrTokenRef = "com.atebits.Tweetie2",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_threads",
            platform = Platform.IOS,
            displayName = "Threads",
            packageOrTokenRef = "com.burbn.barcelona",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_pinterest",
            platform = Platform.IOS,
            displayName = "Pinterest",
            packageOrTokenRef = "pinterest",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_bluesky",
            platform = Platform.IOS,
            displayName = "Bluesky",
            packageOrTokenRef = "xyz.blueskyweb.app",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_twitch",
            platform = Platform.IOS,
            displayName = "Twitch Clips",
            packageOrTokenRef = "tv.twitch",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_tumblr",
            platform = Platform.IOS,
            displayName = "Tumblr",
            packageOrTokenRef = "com.tumblr.tumblr",
            category = CATEGORY_FEED_SCROLLING,
        ),
        TrackedApp(
            id = "ios_imgur",
            platform = Platform.IOS,
            displayName = "Imgur",
            packageOrTokenRef = "com.imgur.imgur",
            category = CATEGORY_FEED_SCROLLING,
        ),
    )

    val allAndroidPackages: Set<String> = defaultApps
        .filter { it.platform == Platform.ANDROID }
        .map { it.packageOrTokenRef }
        .toSet()

    val allIosBundleIds: Set<String> = defaultApps
        .filter { it.platform == Platform.IOS }
        .map { it.packageOrTokenRef }
        .toSet()

    private val packageToAppMap: Map<String, TrackedApp> = defaultApps.associateBy { it.packageOrTokenRef }
    private val idToAppMap: Map<String, TrackedApp> = defaultApps.associateBy { it.id }

    fun getAppsByPlatform(platform: Platform): List<TrackedApp> =
        defaultApps.filter { it.platform == platform }

    fun getAppsByCategory(category: String): List<TrackedApp> =
        defaultApps.filter { it.category == category }

    fun isSupported(packageOrId: String): Boolean =
        packageToAppMap.containsKey(packageOrId) || idToAppMap.containsKey(packageOrId)

    fun isShortVideo(packageOrId: String): Boolean =
        getCategory(packageOrId) == CATEGORY_SHORT_VIDEO

    fun isFeedScrolling(packageOrId: String): Boolean =
        getCategory(packageOrId) == CATEGORY_FEED_SCROLLING

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
