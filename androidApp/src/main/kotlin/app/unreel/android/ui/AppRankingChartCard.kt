package app.unreel.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unreel.model.DailyUsage
import app.unreel.model.TrackedAppCatalog
import kotlin.math.roundToInt

/**
 * Filter, clean, and rank app-specific usage records by reels descending.
 */
fun rankAppUsage(breakdown: List<DailyUsage>): List<DailyUsage> {
    return breakdown
        .filter { it.appId != "aggregate" && (it.reels > 0 || it.durationMs > 0) }
        .sortedWith(
            compareByDescending<DailyUsage> { it.reels }
                .thenByDescending { it.durationMs },
        )
}

/**
 * Computes integer percentage of total reels safely.
 */
fun calculateReelPercentage(reels: Int, totalReels: Int): Int {
    if (totalReels <= 0 || reels <= 0) return 0
    return ((reels.toDouble() / totalReels) * 100).roundToInt().coerceIn(0, 100)
}

/**
 * Computes filled fraction relative to max reels across apps.
 */
fun calculateBarFraction(reels: Int, maxReels: Int): Float {
    if (maxReels <= 0 || reels <= 0) return 0f
    return (reels.toFloat() / maxReels).coerceIn(0f, 1f)
}

/**
 * Returns distinct brand or rank accent color for the platform.
 */
fun getAppAccentColor(appId: String, rank: Int): Color {
    val lower = appId.lowercase()
    return when {
        lower.contains("instagram") || lower.contains("instander") || lower.contains("aeroinsta") || lower.contains("honista") -> Color(0xFFE1306C)
        lower.contains("youtube") || lower.contains("revanced") || lower.contains("rvx") || lower.contains("morphe") -> Color(0xFFFF0000)
        lower.contains("musically") || lower.contains("tiktok") -> Color(0xFF00CEC9)
        lower.contains("snapchat") -> Color(0xFFFFB300)
        lower.contains("facebook") || lower.contains("katana") -> Color(0xFF1877F2)
        lower.contains("reddit") -> Color(0xFFFF4500)
        lower.contains("twitter") || lower.contains("twidere") -> Color(0xFF1DA1F2)
        lower.contains("barcelona") -> Color(0xFF6C5CE7)
        lower.contains("pinterest") -> Color(0xFFE60023)
        lower.contains("mohalla") || lower.contains("video") && lower.contains("moj") -> Color(0xFFFF6B6B)
        lower.contains("eterno") || lower.contains("josh") -> Color(0xFFFF7675)
        lower.contains("kwai") -> Color(0xFFFF7043)
        lower.contains("likee") -> Color(0xFFE040FB)
        lower.contains("lemon8") -> Color(0xFFFFD600)
        else -> when (rank) {
            1 -> Color(0xFF6C5CE7)
            2 -> Color(0xFF00B894)
            3 -> Color(0xFF0984E3)
            4 -> Color(0xFFE17055)
            else -> Color(0xFF636E72)
        }
    }
}

/**
 * Returns container and content colors for top ranking badges.
 */
fun getRankBadgeColors(rank: Int): Pair<Color, Color> {
    return when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.22f) to Color(0xFFD48806) // Gold
        2 -> Color(0xFF90A4AE).copy(alpha = 0.25f) to Color(0xFF37474F) // Silver
        3 -> Color(0xFFCD7F32).copy(alpha = 0.22f) to Color(0xFF795548) // Bronze
        else -> Color(0xFF718096).copy(alpha = 0.12f) to Color(0xFF718096)
    }
}

/**
 * Card displaying a ranked horizontal bar chart of app-specific reel consumption.
 */
@Composable
fun AppRankingChartCard(
    breakdown: List<DailyUsage>,
    title: String = "Reels Watched by App",
    subtitle: String = "Ranked scroll count across platforms",
    modifier: Modifier = Modifier,
) {
    val rankedList = remember(breakdown) { rankAppUsage(breakdown) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (rankedList.isEmpty()) {
                // Empty state
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                    Column {
                        Text(
                            text = "No reel activity recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Reels watched in supported apps will be ranked here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                val maxReels = remember(rankedList) { rankedList.maxOfOrNull { it.reels } ?: 1 }
                val totalReels = remember(rankedList) { rankedList.sumOf { it.reels }.coerceAtLeast(1) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    rankedList.forEachIndexed { index, item ->
                        val rank = index + 1
                        val fraction = calculateBarFraction(item.reels, maxReels)
                        val pct = calculateReelPercentage(item.reels, totalReels)
                        val accentColor = getAppAccentColor(item.appId, rank)
                        val (badgeBg, badgeText) = getRankBadgeColors(rank)
                        val displayName = TrackedAppCatalog.getDisplayName(item.appId)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(badgeBg),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "#$rank",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeText,
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = formatDwellTime(item.durationMs),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "${item.reels}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = accentColor,
                                    )
                                    Text(
                                        text = "reels ($pct%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }

                            // Horizontal Bar Chart Track & Fill
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
