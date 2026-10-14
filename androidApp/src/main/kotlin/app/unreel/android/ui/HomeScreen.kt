package app.unreel.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.unreel.model.Decision
import app.unreel.presentation.home.HomeIntent
import app.unreel.presentation.home.HomeState
import app.unreel.presentation.home.HomeViewModel

val HomeState.isRestricted: Boolean
    get() = currentDecision is Decision.Block || currentDecision is Decision.Friction

fun formatDwellTime(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 && seconds > 0 -> "${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m"
        seconds > 0 -> "${seconds}s"
        else -> "0m"
    }
}

fun calculateGoalProgress(currentReels: Int, targetReels: Int): Float {
    if (targetReels <= 0) return 0f
    return (currentReels.toFloat() / targetReels).coerceIn(0f, 1f)
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun HomeScreenContent(
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // App Header / Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Today's Scroll",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Tracked app: ${state.selectedAppId ?: "All Apps"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            SuggestionChip(
                onClick = { onIntent(HomeIntent.Refresh) },
                label = { Text(state.source.name) },
            )
        }

        // Restriction Banner (if blocked or in friction)
        if (state.isRestricted) {
            RestrictionBanner(
                decision = state.currentDecision,
                onBypass = { onIntent(HomeIntent.RequestBypass) },
            )
        }

        // Today's Usage Summary Card
        TodayUsageCard(
            totalReels = state.totalReels,
            durationMs = state.totalDurationMs,
            goalReels = state.goalReels,
        )

        // Live Active Session Card (if any)
        state.activeSession?.let { session ->
            ActiveSessionCard(
                reelCount = session.reelCount,
                durationMs = session.durationMs,
                appId = session.appId,
            )
        }

        // Focus Score Card
        state.focusScore?.let { focusScore ->
            FocusScoreCard(
                score = focusScore.value,
                components = focusScore.components,
            )
        }
    }
}

@Composable
private fun TodayUsageCard(
    totalReels: Int,
    durationMs: Long,
    goalReels: Int,
    modifier: Modifier = Modifier,
) {
    val progress = calculateGoalProgress(totalReels, goalReels)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = "$totalReels",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "reels watched",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDwellTime(durationMs),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "total time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Daily Goal ($goalReels reels)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = if (progress >= 1f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun ActiveSessionCard(
    reelCount: Int,
    durationMs: Long,
    appId: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00CEC9)),
                )
                Column {
                    Text(
                        text = "Active Session ($appId)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = "$reelCount reels • ${formatDwellTime(durationMs)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusScoreCard(
    score: Int,
    components: app.unreel.model.FocusScoreComponents,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Focus Score",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = when {
                            score >= 80 -> "Excellent focus today"
                            score >= 50 -> "Moderate scroll habits"
                            else -> "High distraction detected"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "$score",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        score >= 80 -> Color(0xFF00B894)
                        score >= 50 -> Color(0xFFFDCB6E)
                        else -> MaterialTheme.colorScheme.error
                    },
                )
            }
        }
    }
}

@Composable
private fun RestrictionBanner(
    decision: Decision,
    onBypass: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (title, reason, canBypass) = when (decision) {
        is Decision.Block -> Triple("Scroll Limit Enforced", decision.reason, decision.canBypass)
        is Decision.Friction -> Triple("Take a Breath", decision.reason, false)
        is Decision.Nudge -> Triple("Gentle Nudge", decision.reason, false)
        Decision.Allow -> Triple("", "", false)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            if (canBypass) {
                OutlinedButton(
                    onClick = onBypass,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text("Use 5-min Emergency Pass")
                }
            }
        }
    }
}
