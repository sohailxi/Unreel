package app.unreel.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.unreel.model.Confidence
import app.unreel.model.DailyUsage
import app.unreel.model.DataSource
import app.unreel.model.TrendDirection
import app.unreel.model.TrendResult
import app.unreel.presentation.stats.StatsIntent
import app.unreel.presentation.stats.StatsState
import app.unreel.presentation.stats.StatsViewModel

fun formatTrendPercent(percent: Double): String {
    val sign = if (percent > 0) "+" else ""
    return "$sign%.1f%%".format(percent)
}

fun formatTrendDescription(trend: TrendResult): String {
    if (trend.confidence == Confidence.LOW) {
        return "Not enough data"
    }
    val directionLabel = when (trend.direction) {
        TrendDirection.IMPROVING -> "Improving"
        TrendDirection.SLIPPING -> "Slipping"
        TrendDirection.STEADY -> "Steady"
    }
    return "$directionLabel (${formatTrendPercent(trend.percentChange)})"
}

fun formatSessionsAndOpens(sessions: Int, opens: Int): String {
    val sesText = "$sessions ses"
    val openText = if (opens == 1) "$opens open" else "$opens opens"
    return "$sesText • $openText"
}

fun getStatsAppLabel(selectedAppId: String?): String =
    selectedAppId?.let { app.unreel.model.TrackedAppCatalog.getDisplayName(it) } ?: "All Feeds & Reels"

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    StatsScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun StatsScreenContent(
    state: StatsState,
    onIntent: (StatsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Time Range Selector
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(7, 14, 30).forEach { days ->
                    FilterChip(
                        selected = state.selectedDays == days,
                        onClick = { onIntent(StatsIntent.SelectTimeRange(days)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        label = { Text("${days}d") },
                    )
                }
            }
        }

        // Trend Result Card
        item {
            TrendCard(trend = state.trend)
        }

        // Averages Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = "${state.averageDailyReels}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                            )
                        }
                        Text(
                            text = "Daily Avg Reels",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = formatDwellTime(state.averageDailyDurationMs),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                        }
                        Text(
                            text = "Daily Avg Time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
        }

        // App Ranking by Reels Chart Card for selected time range
        item {
            AppRankingChartCard(
                breakdown = state.appBreakdown,
                title = "App Ranking (${state.selectedDays}d)",
                subtitle = "Cumulative reels watched per app by ranking",
            )
        }

        // Reels Watched by App Ranking Chart Card
        if (state.appBreakdown.isNotEmpty()) {
            item {
                AppRankingChartCard(
                    breakdown = state.appBreakdown,
                    title = "Reels Watched by App (${state.selectedDays}d)",
                    subtitle = "Ranked consumption for the selected period",
                )
            }
        }

        // Daily History Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Daily Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (state.dailyHistory.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "No history recorded yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(state.dailyHistory) { daily ->
                DailyHistoryRow(daily = daily)
            }
        }
    }
}

@Composable
private fun TrendCard(
    trend: TrendResult,
    modifier: Modifier = Modifier,
) {
    val trendIcon = when (trend.direction) {
        TrendDirection.IMPROVING -> Icons.AutoMirrored.Rounded.TrendingDown
        TrendDirection.SLIPPING -> Icons.AutoMirrored.Rounded.TrendingUp
        TrendDirection.STEADY -> Icons.AutoMirrored.Rounded.TrendingFlat
    }

    val trendColor = when (trend.direction) {
        TrendDirection.IMPROVING -> Color(0xFF00B894)
        TrendDirection.SLIPPING -> MaterialTheme.colorScheme.error
        TrendDirection.STEADY -> MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "14-Day Trajectory",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = formatTrendDescription(trend),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = trendColor,
                        maxLines = 1,
                    )
                }
                SuggestionChip(
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = if (trend.confidence == Confidence.HIGH) {
                                Icons.Rounded.Verified
                            } else {
                                Icons.AutoMirrored.Rounded.HelpOutline
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                    },
                    label = { Text(trend.confidence.name, fontSize = 11.sp) },
                )
            }
        }
    }
}

@Composable
private fun DailyHistoryRow(
    daily: DailyUsage,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = daily.localDate.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Text(
                    text = formatSessionsAndOpens(daily.sessions, daily.opens),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${daily.reels} reels",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = formatDwellTime(daily.durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

