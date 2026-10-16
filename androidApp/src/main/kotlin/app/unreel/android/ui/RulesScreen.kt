package app.unreel.android.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.unreel.android.platform.PermissionManager
import app.unreel.android.platform.PermissionStatus
import app.unreel.android.service.ReelDetectorService
import app.unreel.android.service.UnreelForegroundService
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.Strictness
import app.unreel.presentation.rules.RulesIntent
import app.unreel.presentation.rules.RulesState
import app.unreel.presentation.rules.RulesViewModel
import org.koin.compose.koinInject

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

fun ruleKindIcon(kind: RuleKind): ImageVector {
    return when (kind) {
        RuleKind.DAILY_REEL_LIMIT -> Icons.Rounded.VideoLibrary
        RuleKind.DAILY_TIME_LIMIT -> Icons.Rounded.Timer
        RuleKind.SESSION_CAP -> Icons.Rounded.HourglassTop
        RuleKind.SCHEDULE_WINDOW -> Icons.Rounded.Bedtime
        RuleKind.OPEN_COUNT_LIMIT -> Icons.Rounded.LockClock
        RuleKind.SHARED_BUDGET -> Icons.Rounded.PieChart
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

/**
 * Determines whether a given [Strictness] setting requires system Overlay (Draw over other apps)
 * permission to enforce visual blocking or friction shields.
 */
fun requiresOverlayShield(strictness: Strictness): Boolean =
    strictness != Strictness.NUDGE

@Composable
fun RulesScreen(
    viewModel: RulesViewModel,
    permissionManager: PermissionManager = koinInject(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isServiceRunning by UnreelForegroundService.isRunning.collectAsStateWithLifecycle()
    val isDetectorConnected by ReelDetectorService.isConnected.collectAsStateWithLifecycle()
    var permissionStatus by remember { mutableStateOf(permissionManager.getPermissionStatus()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, isServiceRunning, isDetectorConnected) {
        permissionStatus = permissionManager.getPermissionStatus()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = permissionManager.getPermissionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    RulesScreenContent(
        state = state,
        permissionStatus = permissionStatus,
        onIntent = viewModel::onIntent,
        onEnableAccessibility = { permissionManager.openAccessibilitySettings() },
        onGrantOverlay = { permissionManager.openOverlaySettings() },
        onOpenAutostart = { permissionManager.openAutostartSettings() },
        onToggleService = {
            if (permissionStatus.isServiceRunning) {
                permissionManager.stopMonitoringService()
            } else {
                permissionManager.startMonitoringService()
            }
            permissionStatus = permissionManager.getPermissionStatus()
        },
        modifier = modifier,
    )
}

@Composable
fun RulesScreenContent(
    state: RulesState,
    permissionStatus: PermissionStatus = PermissionStatus(),
    onIntent: (RulesIntent) -> Unit = {},
    onEnableAccessibility: () -> Unit = {},
    onGrantOverlay: () -> Unit = {},
    onOpenAutostart: () -> Unit = {},
    onToggleService: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var pendingRuleToEnable by remember { mutableStateOf<LimitRule?>(null) }
    var showOverlayDialog by remember { mutableStateOf(false) }

    if (showOverlayDialog) {
        AlertDialog(
            onDismissRequest = {
                showOverlayDialog = false
                pendingRuleToEnable = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
            },
            title = {
                Text(
                    text = "Overlay Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                val ruleName = pendingRuleToEnable?.let { formatRuleKind(it.kind) } ?: "This limit rule"
                Text(
                    text = "$ruleName enforces limits by displaying a friction shield or block screen over short videos. Android requires 'Draw over other apps' permission to display shields.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOverlayDialog = false
                        pendingRuleToEnable = null
                        onGrantOverlay()
                    },
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showOverlayDialog = false
                        pendingRuleToEnable = null
                    },
                ) {
                    Text("Cancel")
                }
            },
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Tune,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
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
        }

        // System Enforcement Protection Card
        item {
            SystemEnforcementProtectionCard(
                status = permissionStatus,
                onEnableAccessibility = onEnableAccessibility,
                onGrantOverlay = onGrantOverlay,
                onOpenAutostart = onOpenAutostart,
                onToggleService = onToggleService,
            )
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Focus Target Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // Target Reels
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
                                imageVector = Icons.Rounded.VideoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column {
                                Text(
                                    text = "Daily Reel Goal",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${state.goalReels} reels per day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledTonalIconButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalReels((state.goalReels - 10).coerceAtLeast(10))) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease reels", modifier = Modifier.size(18.dp))
                            }
                            FilledTonalIconButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalReels(state.goalReels + 10)) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase reels", modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Target Minutes
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
                                imageVector = Icons.Rounded.Timer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                            Column {
                                Text(
                                    text = "Daily Time Goal",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${state.goalMinutes} minutes per day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            FilledTonalIconButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalMinutes((state.goalMinutes - 5).coerceAtLeast(5))) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Rounded.Remove, contentDescription = "Decrease time", modifier = Modifier.size(18.dp))
                            }
                            FilledTonalIconButton(
                                onClick = { onIntent(RulesIntent.UpdateFocusGoalMinutes(state.goalMinutes + 5)) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Rounded.Add, contentDescription = "Increase time", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Active Rules Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "Active Rules (${state.rules.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (state.rules.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Text(
                            text = "No custom rules configured yet. Goals above are active.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        } else {
            items(state.rules) { rule ->
                RuleCard(
                    rule = rule,
                    onToggle = { enabled ->
                        if (enabled && requiresOverlayShield(rule.strictness) && !permissionStatus.isOverlayGranted) {
                            pendingRuleToEnable = rule
                            showOverlayDialog = true
                        } else {
                            onIntent(RulesIntent.ToggleRule(rule.id, enabled))
                        }
                    },
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
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = ruleKindIcon(rule.kind),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(24.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = formatRuleKind(rule.kind),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    SuggestionChip(
                        onClick = {},
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Security,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                            )
                        },
                        label = { Text(rule.strictness.name, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    )
                    Text(
                        text = formatRuleParamsSummary(rule),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val appsDisplay = if (rule.appIds.isEmpty()) {
                        "All short video & scrolling platforms"
                    } else {
                        rule.appIds.joinToString { app.unreel.model.TrackedAppCatalog.getDisplayName(it) }
                    }
                    Text(
                        text = "Apps: $appsDisplay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = onToggle,
            )
        }
    }
}

@Composable
private fun SystemEnforcementProtectionCard(
    status: PermissionStatus,
    onEnableAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit,
    onOpenAutostart: () -> Unit = {},
    onToggleService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
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
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = "System Protection & Shielding",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (status.isOverlayGranted && status.isAccessibilityEnabled && status.isServiceRunning) {
                    SuggestionChip(
                        onClick = {},
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF00B894),
                            )
                        },
                        label = { Text("Active", fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF00B894).copy(alpha = 0.15f),
                            labelColor = Color(0xFF00B894),
                        ),
                    )
                }
            }

            Text(
                text = "Enforce limits by blocking short video apps with floating shields. Android requires Overlay and Accessibility permissions to operate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Overlay Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = if (status.isOverlayGranted) Color(0xFF00B894) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Screen Blocking Shield",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (status.isOverlayGranted) {
                    Text("Granted ✓", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00B894))
                } else {
                    FilledTonalButton(
                        onClick = onGrantOverlay,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp, minWidth = 1.dp),
                    ) {
                        Text("Grant Permission", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            // Accessibility Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        tint = if (status.isAccessibilityEnabled) Color(0xFF00B894) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Reel Detection Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (status.isAccessibilityEnabled) {
                    Text("Active ✓", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00B894))
                } else {
                    FilledTonalButton(
                        onClick = onEnableAccessibility,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp, minWidth = 1.dp),
                    ) {
                        Text("Enable Detector", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }

            // Foreground Service Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = if (status.isServiceRunning) Color(0xFF00B894) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = "Ongoing Monitoring Service",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                FilledTonalButton(
                    onClick = onToggleService,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp, minWidth = 1.dp),
                    colors = if (status.isServiceRunning) {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    } else {
                        ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        )
                    },
                ) {
                    Text(if (status.isServiceRunning) "Stop Service" else "Start Service", fontSize = 11.sp, maxLines = 1)
                }
            }

            // OEM Autostart Row (if device is an aggressive OEM)
            if (status.isAggressiveOem) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Background Autostart (${status.deviceManufacturer})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

                    FilledTonalButton(
                        onClick = onOpenAutostart,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp, minWidth = 1.dp),
                    ) {
                        Text("Configure", fontSize = 11.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

