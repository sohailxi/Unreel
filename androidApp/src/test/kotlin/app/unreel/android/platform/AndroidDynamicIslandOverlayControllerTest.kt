package app.unreel.android.platform

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidDynamicIslandOverlayControllerTest {

    @Test
    fun `controller handles null context gracefully without throwing`() {
        val controller = AndroidDynamicIslandOverlayController(context = null)
        assertFalse(controller.isVisible())
        assertFalse(controller.isExpanded())

        controller.showOrUpdate(
            sessionReels = 5,
            todayReels = 20,
            todayDurationMs = 60_000L,
            limitProgress = 0.5f,
        )

        assertFalse(controller.isVisible())

        controller.hide()
        assertFalse(controller.isVisible())
    }

    @Test
    fun `fake controller tracks state changes accurately`() {
        val fake = FakeDynamicIslandOverlayController()
        assertFalse(fake.isVisible())

        fake.showOrUpdate(
            sessionReels = 12,
            todayReels = 45,
            todayDurationMs = 180_000L,
            limitProgress = 0.75f,
        )

        assertTrue(fake.isVisible())
        assertEquals(12, fake.lastSessionReels)
        assertEquals(45, fake.lastTodayReels)
        assertEquals(180_000L, fake.lastTodayDurationMs)
        assertEquals(0.75f, fake.lastLimitProgress)

        fake.setExpanded(true)
        assertTrue(fake.isExpanded())

        fake.hide()
        assertFalse(fake.isVisible())
        assertFalse(fake.isExpanded())
    }

    private class FakeDynamicIslandOverlayController : DynamicIslandOverlayController {
        private var visible: Boolean = false
        private var expanded: Boolean = false
        var lastSessionReels: Int = 0
        var lastTodayReels: Int = 0
        var lastTodayDurationMs: Long = 0L
        var lastLimitProgress: Float? = null

        override fun showOrUpdate(
            sessionReels: Int,
            todayReels: Int,
            todayDurationMs: Long,
            limitProgress: Float?,
        ) {
            visible = true
            lastSessionReels = sessionReels
            lastTodayReels = todayReels
            lastTodayDurationMs = todayDurationMs
            lastLimitProgress = limitProgress
        }

        override fun hide() {
            visible = false
            expanded = false
        }

        override fun isVisible(): Boolean = visible

        override fun isExpanded(): Boolean = expanded

        override fun setExpanded(expanded: Boolean) {
            this.expanded = expanded
        }
    }
}
