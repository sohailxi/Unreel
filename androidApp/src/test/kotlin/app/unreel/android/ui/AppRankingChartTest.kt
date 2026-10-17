package app.unreel.android.ui

import androidx.compose.ui.graphics.Color
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppRankingChartTest {

    private val testDate = LocalDate(2026, 10, 16)

    @Test
    fun `rankAppUsage filters out zero activity and aggregates and sorts descending`() {
        val list = listOf(
            DailyUsage(testDate, "com.instagram.android", reels = 25, durationMs = 600_000L, sessions = 2, opens = 2, source = DataSource.EXACT),
            DailyUsage(testDate, "aggregate", reels = 95, durationMs = 2_100_000L, sessions = 6, opens = 6, source = DataSource.EXACT),
            DailyUsage(testDate, "com.google.android.youtube", reels = 60, durationMs = 1_200_000L, sessions = 3, opens = 3, source = DataSource.EXACT),
            DailyUsage(testDate, "com.zhiliaoapp.musically", reels = 10, durationMs = 300_000L, sessions = 1, opens = 1, source = DataSource.EXACT),
            DailyUsage(testDate, "com.snapchat.android", reels = 0, durationMs = 0L, sessions = 0, opens = 0, source = DataSource.EXACT),
        )

        val ranked: List<DailyUsage> = rankAppUsage(list)

        assertEquals(3, ranked.size)
        assertEquals("com.google.android.youtube", ranked[0].appId)
        assertEquals(60, ranked[0].reels)
        assertEquals("com.instagram.android", ranked[1].appId)
        assertEquals(25, ranked[1].reels)
        assertEquals("com.zhiliaoapp.musically", ranked[2].appId)
        assertEquals(10, ranked[2].reels)
    }

    @Test
    fun `calculateReelPercentage handles zero and division properly`() {
        assertEquals(0, calculateReelPercentage(0, 0))
        assertEquals(0, calculateReelPercentage(0, 100))
        assertEquals(50, calculateReelPercentage(50, 100))
        assertEquals(33, calculateReelPercentage(1, 3))
        assertEquals(100, calculateReelPercentage(50, 50))
    }

    @Test
    fun `calculateBarFraction returns proportion bounded between zero and one`() {
        assertEquals(0f, calculateBarFraction(0, 0))
        assertEquals(0.5f, calculateBarFraction(50, 100))
        assertEquals(1.0f, calculateBarFraction(100, 100))
        assertEquals(1.0f, calculateBarFraction(150, 100))
    }

    @Test
    fun `getAppAccentColor returns specific colors for key video apps and falls back gracefully`() {
        val igColor = getAppAccentColor("com.instagram.android", rank = 1)
        val ytColor = getAppAccentColor("com.google.android.youtube", rank = 1)
        val ttColor = getAppAccentColor("com.zhiliaoapp.musically", rank = 1)
        val otherColor = getAppAccentColor("unknown.app", rank = 1)

        assertTrue(igColor != ytColor)
        assertTrue(ytColor != ttColor)
        assertEquals(Color(0xFFE1306C), igColor)
        assertEquals(Color(0xFFFF0000), ytColor)
        assertEquals(Color(0xFF00CEC9), ttColor)
        assertTrue(otherColor != Color.Unspecified)
    }
}
