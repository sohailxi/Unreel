package app.unreel.android.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import app.unreel.android.MainActivity
import app.unreel.data.snapshot.SettingsSnapshotSink
import app.unreel.data.snapshot.UsageSnapshot
import app.unreel.model.DataSource
import kotlinx.serialization.json.Json

/**
 * Presentation model for [UnreelGlanceWidget].
 */
data class UnreelGlanceWidgetState(
    val todayReels: Int,
    val formattedDuration: String,
    val sessionReels: Int,
    val isSessionActive: Boolean,
    val limitProgress: Float?,
    val isBlocked: Boolean,
    val statusText: String,
    val source: DataSource,
)

/**
 * Formats duration in milliseconds into a concise string suitable for home screen widgets.
 */
fun formatGlanceTime(durationMs: Long): String {
    val totalMinutes = durationMs / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

private val glanceJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * Deserializes [UsageSnapshot] from raw JSON string, returning null if invalid or corrupt.
 */
fun parseSnapshot(jsonString: String?): UsageSnapshot? {
    if (jsonString.isNullOrBlank()) return null
    return try {
        glanceJson.decodeFromString<UsageSnapshot>(jsonString)
    } catch (_: Exception) {
        null
    }
}

/**
 * Maps a [UsageSnapshot] into displayable widget state.
 */
fun createWidgetState(snapshot: UsageSnapshot?): UnreelGlanceWidgetState {
    if (snapshot == null) {
        return UnreelGlanceWidgetState(
            todayReels = 0,
            formattedDuration = "0m",
            sessionReels = 0,
            isSessionActive = false,
            limitProgress = null,
            isBlocked = false,
            statusText = "Within limits",
            source = DataSource.EXACT,
        )
    }

    val limitProgress = snapshot.limitProgress
    val status = when {
        snapshot.isBlocked -> snapshot.decisionReason ?: "Limit reached"
        limitProgress != null && limitProgress >= 0.8f -> "Nearing limit"
        else -> "Within limits"
    }

    return UnreelGlanceWidgetState(
        todayReels = snapshot.todayReels,
        formattedDuration = formatGlanceTime(snapshot.todayDurationMs),
        sessionReels = snapshot.sessionReels,
        isSessionActive = snapshot.sessionReels > 0,
        limitProgress = snapshot.limitProgress,
        isBlocked = snapshot.isBlocked,
        statusText = status,
        source = snapshot.source,
    )
}

/**
 * Glance App Widget for Unreel home screen tracking.
 */
class UnreelGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("unreel_preferences", Context.MODE_PRIVATE)
        val snapshotJson = prefs.getString(SettingsSnapshotSink.KEY_USAGE_SNAPSHOT, null)
        val snapshot = parseSnapshot(snapshotJson)
        val state = createWidgetState(snapshot)

        provideContent {
            GlanceTheme {
                UnreelWidgetContent(state = state)
            }
        }
    }

    @Composable
    private fun UnreelWidgetContent(state: UnreelGlanceWidgetState) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .cornerRadius(16.dp)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "UNREEL",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Box(
                    modifier = GlanceModifier
                        .background(
                            if (state.isBlocked) GlanceTheme.colors.errorContainer
                            else GlanceTheme.colors.surfaceVariant
                        )
                        .cornerRadius(6.dp)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = if (state.isBlocked) "BLOCKED" else state.source.name,
                        style = TextStyle(
                            color = if (state.isBlocked) GlanceTheme.colors.onErrorContainer
                            else GlanceTheme.colors.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Primary Metrics Row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "${state.todayReels}",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        ),
                    )
                    Text(
                        text = "reels today",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp,
                        ),
                    )
                }

                Spacer(modifier = GlanceModifier.width(20.dp))

                Column {
                    Text(
                        text = state.formattedDuration,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                        ),
                    )
                    Text(
                        text = "time spent",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Limit Progress Bar
            if (state.limitProgress != null) {
                LinearProgressIndicator(
                    progress = state.limitProgress,
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .cornerRadius(3.dp),
                    color = if (state.isBlocked) GlanceTheme.colors.error
                    else GlanceTheme.colors.primary,
                    backgroundColor = GlanceTheme.colors.surfaceVariant,
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
            }

            // Footer Row (Session & Status)
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = state.statusText,
                    style = TextStyle(
                        color = if (state.isBlocked) GlanceTheme.colors.error
                        else GlanceTheme.colors.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                    ),
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                if (state.isSessionActive) {
                    Text(
                        text = "Session: ${state.sessionReels}",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                    )
                }
            }
        }
    }
}
