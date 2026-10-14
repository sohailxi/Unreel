package app.unreel.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.Strictness
import app.unreel.presentation.rules.RulesIntent
import app.unreel.presentation.rules.RulesState
import app.unreel.presentation.rules.RulesViewModel

fun formatRuleKind(kind: RuleKind): String {
    return when (kind) {
        RuleKind.DAILY_TIME_LIMIT -> "Daily Time Limit"
        RuleKind.DAILY_REEL_LIMIT -> "Daily Reel Limit"
        RuleKind.SESSION_CAP -> "Session Cap"
        RuleKind.SCHEDULE_WINDOW -> "Schedule Window"
        RuleKind.OPEN_COUNT_LIMIT -> "Open Count Limit"
        RuleKind.SHARED_BUDGET -> "Shared Budget"
    }
}

fun formatRuleParamsSummary(rule: LimitRule): String {
    val p = rule.params
    return when {
        p.limitReels != null -> "${p.limitReels} reels limit"
        p.limitMinutes != null -> "${p.limitMinutes} minutes limit"
        p.maxOpens != null -> "${p.maxOpens} maximum opens"
        else -> "Custom rule parameter"
    }
}

@Composable
fun RulesScreen(
    viewModel: RulesViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    RulesScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@Composable
fun RulesScreenContent(
    state: RulesState,
    onIntent: (RulesIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Limits & Goals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Control scroll time and set strictness",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Daily Goals Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "Focus Target Goals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    // Target Reels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Daily Reel Goal",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${state.goalReels} reels per day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalReels((state.goalReels - 10).coerceAtLeast(10))) },
                            ) {
                                Text("-10")
                            }
                            FilledTonalButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalReels(state.goalReels + 10)) },
                            ) {
                                Text("+10")
                            }
                        }
                    }

                    // Target Minutes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Daily Time Goal",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${state.goalMinutes} minutes per day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalMinutes((state.goalMinutes - 5).coerceAtLeast(5))) },
                            ) {
                                Text("-5m")
                            }
                            FilledTonalButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalMinutes(state.goalMinutes + 5)) },
                            ) {
                                Text("+5m")
                            }
                        }
                    }
                }
            }
        }

        // Active Rules Section Header
        item {
            Text(
                text = "Active Rules (${state.rules.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        if (state.rules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Text(
                        text = "No custom rules configured yet. Goals above are active.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.rules) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { enabled -> onIntent(RulesIntent.ToggleRule(rule.id, enabled)) },
                )
            }
        }
    }
}

@Composable
private fun RuleCard(
    rule: LimitRule,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = formatRuleKind(rule.kind),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(rule.strictness.name) },
                    )
                }
                Text(
                    text = formatRuleParamsSummary(rule),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Apps: ${rule.appIds.joinToString()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}
