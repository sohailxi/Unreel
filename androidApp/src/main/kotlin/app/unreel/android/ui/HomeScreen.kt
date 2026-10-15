package app.unreel.android.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import app.unreel.model.DataSource
import app.unreel.model.Decision
import app.unreel.presentation.home.HomeIntent
import app.unreel.presentation.home.HomeState
import app.unreel.presentation.home.HomeViewModel

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.unreel.android.platform.PermissionManager
import app.unreel.android.platform.PermissionStatus
import app.unreel.android.service.ReelDetectorService
import app.unreel.android.service.UnreelForegroundService
import org.koin.compose.koinInject

val HomeState.isRestricted: Boolean
    get() = currentDecision is Decision.Block || currentDecision is Decision.Friction

fun getServiceStatusHeader(status: PermissionStatus): Pair<String, String> {
    return when {
        status.isFullyConfigured -> "Active Protection" to "Reel monitoring and shield enforcement active"
        status.areCorePermissionsGranted && !status.isServiceRunning -> "Service Inactive" to "Tap Start to resume scroll tracking"
        status.isServiceRunning -> "Permissions Required" to "Grant permissions below to enable tracking"
        else -> "Setup Required" to "Start service and grant permissions to track reels"
    }
}

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

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) {
        permissionStatus = permissionManager.getPermissionStatus()
    }

    HomeScreenContent(
        state = state,
        permissionStatus = permissionStatus,
        onIntent = viewModel::onIntent,
        onEnableAccessibility = { permissionManager.openAccessibilitySettings() },
        onGrantOverlay = { permissionManager.openOverlaySettings() },
        onRequestNotification = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                permissionManager.openNotificationSettings()
            }
        },
        onSendTestNotification = { permissionManager.sendTestNotification() },
        onRequestIgnoreBatteryOptimization = { permissionManager.requestIgnoreBatteryOptimizations() },
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
fun HomeScreenContent(
    state: HomeState,
    permissionStatus: PermissionStatus = PermissionStatus(),
    onIntent: (HomeIntent) -> Unit = {},
    onEnableAccessibility: () -> Unit = {},
    onGrantOverlay: () -> Unit = {},
    onRequestNotification: () -> Unit = {},
    onSendTestNotification: () -> Unit = {},
    onRequestIgnoreBatteryOptimization: () -> Unit = {},
    onOpenAutostart: () -> Unit = {},
    onToggleService: () -> Unit = {},
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SuggestionChip(
                    onClick = { onIntent(HomeIntent.Refresh) },
                    icon = {
                        Icon(
                            imageVector = if (state.source == DataSource.EXACT) {
                                Icons.Rounded.Verified
                            } else {
                                Icons.Rounded.Info
                            },
                            contentDescription = state.source.name,
                            modifier = Modifier.size(16.dp),
                            tint = if (state.source == DataSource.EXACT) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            },
                        )
                    },
                    label = { Text(state.source.name) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                )
                IconButton(onClick = { onIntent(HomeIntent.Refresh) }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh data",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Service & Permissions Status Card
        ServiceStatusCard(
            status = permissionStatus,
            onEnableAccessibility = onEnableAccessibility,
            onGrantOverlay = onGrantOverlay,
            onRequestNotification = onRequestNotification,
            onSendTestNotification = onSendTestNotification,
            onRequestIgnoreBatteryOptimization = onRequestIgnoreBatteryOptimization,
            onOpenAutostart = onOpenAutostart,
            onToggleService = onToggleService,
        )

        // Restriction Banner (if blocked or in friction)
        AnimatedVisibility(
            visible = state.isRestricted,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VideoLibrary,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            text = "$totalReels",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = "reels watched",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = formatDwellTime(durationMs),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Daily Goal ($goalReels reels)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Smartphone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "Active Session • ${app.unreel.model.TrackedAppCatalog.getDisplayName(appId)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Psychology,
                        contentDescription = "Focus Score",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
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

    val bannerIcon = when (decision) {
        is Decision.Block -> Icons.Rounded.Block
        is Decision.Friction -> Icons.Rounded.SelfImprovement
        is Decision.Nudge -> Icons.Rounded.NotificationsActive
        Decision.Allow -> Icons.Rounded.CheckCircle
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = bannerIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
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
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Use 5-min Emergency Pass")
                }
            }
        }
    }
}

@Composable
fun ServiceStatusCard(
    status: PermissionStatus,
    onEnableAccessibility: () -> Unit,
    onGrantOverlay: () -> Unit,
    onRequestNotification: () -> Unit,
    onSendTestNotification: () -> Unit = {},
    onRequestIgnoreBatteryOptimization: () -> Unit,
    onOpenAutostart: () -> Unit = {},
    onToggleService: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (title, description) = getServiceStatusHeader(status)
    var isExpanded by remember(status.isFullyConfigured) { mutableStateOf(!status.isFullyConfigured) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status.isFullyConfigured) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header Row
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
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    status.isServiceRunning && status.isAccessibilityEnabled -> Color(0xFF00B894)
                                    status.isServiceRunning -> Color(0xFFFDCB6E)
                                    else -> MaterialTheme.colorScheme.error
                                },
                            ),
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse details" else "Expand details",
                    )
                }
            }

            // Quick Service Control Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = null,
                        tint = if (status.isServiceRunning) Color(0xFF00B894) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = if (status.isServiceRunning) "Foreground Monitor: Active" else "Foreground Monitor: Stopped",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                FilledTonalButton(
                    onClick = onToggleService,
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
                    Icon(
                        imageVector = if (status.isServiceRunning) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (status.isServiceRunning) "Stop" else "Start Service")
                }
            }

            // Expanded Permissions Checklist
            if (isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // 1. Accessibility Service
                    PermissionRow(
                        title = "Reel Detection (Accessibility)",
                        description = "Detects when you view reels on Instagram, YouTube & TikTok. Privacy-safe: never reads on-screen text or messages.",
                        icon = Icons.Rounded.Visibility,
                        isGranted = status.isAccessibilityEnabled,
                        actionLabel = "Enable in Settings",
                        onAction = onEnableAccessibility,
                    )

                    // 2. Overlay Permission
                    PermissionRow(
                        title = "Screen Blocking Overlay",
                        description = "Required to display the blocking shield over reels when your daily limit is reached.",
                        icon = Icons.Rounded.Layers,
                        isGranted = status.isOverlayGranted,
                        actionLabel = "Grant Overlay",
                        onAction = onGrantOverlay,
                    )

                    // 3. Notification Permission
                    PermissionRow(
                        title = "Notifications",
                        description = "Shows continuous reel counters in the status bar and sends nudges.",
                        icon = Icons.Rounded.Notifications,
                        isGranted = status.isNotificationGranted,
                        actionLabel = "Allow",
                        onAction = onRequestNotification,
                    )

                    if (status.isNotificationGranted) {
                        OutlinedButton(
                            onClick = onSendTestNotification,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Test Nudge", fontSize = 12.sp)
                        }
                    }

                    // 4. Battery Optimization
                    PermissionRow(
                        title = "Battery Saver Exemption",
                        description = "Prevents aggressive OEM battery managers from stopping the background service.",
                        icon = Icons.Rounded.BatteryChargingFull,
                        isGranted = status.isBatteryOptimizationIgnored,
                        actionLabel = "Disable Saver",
                        onAction = onRequestIgnoreBatteryOptimization,
                    )

                    // 5. OEM Autostart (if applicable)
                    if (status.isAggressiveOem) {
                        PermissionRow(
                            title = "Background Autostart (${status.deviceManufacturer})",
                            description = "Required on ${status.deviceManufacturer} devices to keep the scroll counter from being killed in the background.",
                            icon = Icons.Rounded.PowerSettingsNew,
                            isGranted = status.isBatteryOptimizationIgnored,
                            actionLabel = "Enable Autostart",
                            onAction = onOpenAutostart,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) Color(0xFF00B894) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                if (isGranted) {
                    SuggestionChip(
                        onClick = onAction,
                        icon = {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF00B894),
                            )
                        },
                        label = { Text("Active") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF00B894).copy(alpha = 0.15f),
                            labelColor = Color(0xFF00B894),
                        ),
                    )
                } else {
                    FilledTonalButton(
                        onClick = onAction,
                        modifier = Modifier.height(34.dp),
                    ) {
                        Text(actionLabel, fontSize = 12.sp)
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

