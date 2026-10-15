package app.unreel.android.receiver

import android.content.Intent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BootReceiverTest {

    @Test
    fun `isSupportedAction returns true for BOOT_COMPLETED and MY_PACKAGE_REPLACED`() {
        assertTrue(BootReceiver.isSupportedAction(Intent.ACTION_BOOT_COMPLETED))
        assertTrue(BootReceiver.isSupportedAction(Intent.ACTION_MY_PACKAGE_REPLACED))
    }

    @Test
    fun `isSupportedAction returns false for other actions or null`() {
        assertFalse(BootReceiver.isSupportedAction("android.intent.action.BATTERY_LOW"))
        assertFalse(BootReceiver.isSupportedAction("android.intent.action.SCREEN_ON"))
        assertFalse(BootReceiver.isSupportedAction(null))
    }
}
