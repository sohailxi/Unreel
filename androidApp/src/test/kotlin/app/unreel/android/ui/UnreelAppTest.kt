package app.unreel.android.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class UnreelAppTest {

    @Test
    fun `getTopAppBarTitle returns appropriate clean titles for each tab`() {
        assertEquals("Unreel", getTopAppBarTitle(AppTab.HOME))
        assertEquals("Trends", getTopAppBarTitle(AppTab.STATS))
        assertEquals("Limits & Rules", getTopAppBarTitle(AppTab.RULES))
    }
}
