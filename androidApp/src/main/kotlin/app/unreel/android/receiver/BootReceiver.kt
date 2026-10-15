package app.unreel.android.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.unreel.android.service.UnreelForegroundService
import co.touchlab.kermit.Logger

/**
 * BroadcastReceiver responsible for restarting [UnreelForegroundService]
 * upon device boot or app package update (ADR-005 OEM Survival).
 */
class BootReceiver : BroadcastReceiver() {

    private val logger = Logger.withTag("BootReceiver")

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val action = intent.action
        if (isSupportedAction(action)) {
            logger.i { "Received boot/replacement action: $action. Starting UnreelForegroundService." }
            try {
                UnreelForegroundService.start(context)
            } catch (t: Throwable) {
                logger.e(t) { "Failed to start UnreelForegroundService from BootReceiver" }
            }
        }
    }

    companion object {
        fun isSupportedAction(action: String?): Boolean {
            return action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED
        }
    }
}
