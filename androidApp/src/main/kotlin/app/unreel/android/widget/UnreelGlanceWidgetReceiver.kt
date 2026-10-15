package app.unreel.android.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * BroadcastReceiver responsible for managing lifecycle and updates for [UnreelGlanceWidget].
 */
class UnreelGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = UnreelGlanceWidget()
}
