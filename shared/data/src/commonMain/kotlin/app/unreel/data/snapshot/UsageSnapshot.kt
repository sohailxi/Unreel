package app.unreel.data.snapshot

import app.unreel.model.DataSource
import kotlinx.serialization.Serializable

/**
 * Lightweight, versioned snapshot designed for fast decoding by iOS extensions
 * (WidgetKit, ActivityKit) and Android Glance widgets without linking full KMP or SQLite.
 */
@Serializable
data class UsageSnapshot(
    val version: Int = 1,
    val date: String,
    val todayReels: Int,
    val todayDurationMs: Long,
    val sessionReels: Int,
    val activeSessionDurationMs: Long,
    val limitProgress: Float?,
    val decisionReason: String?,
    val isBlocked: Boolean,
    val source: DataSource,
    val updatedAtUtc: String,
)
