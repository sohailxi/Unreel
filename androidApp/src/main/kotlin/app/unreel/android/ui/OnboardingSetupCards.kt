package app.unreel.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unreel.android.platform.PermissionStatus

@Composable
fun WelcomePrivacyFeaturesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PrivacyFeatureRow(
                icon = Icons.Rounded.Lock,
                title = "Zero Personal Data",
                description = "Never reads or stores chat messages, account credentials, or video content.",
            )
            PrivacyFeatureRow(
                icon = Icons.Rounded.Security,
                title = "100% On-Device",
                description = "Usage metrics, rules, and aggregates remain strictly on your phone.",
            )
            PrivacyFeatureRow(
                icon = Icons.Rounded.PowerSettingsNew,
                title = "You Are In Control",
                description = "Pause, resume, or customize scroll limits and friction shields at any time.",
            )
        }
    }
}

@Composable
fun PrivacyFeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun AccessibilitySetupCard(
    isGranted: Boolean,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        ),
        shape = RoundedCornerShape(16.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = "Accessibility Service",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Accessibility Service",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (isGranted) "Enabled" else "Required",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = if (isGranted) {
                            Color(0xFF2E7D32).copy(alpha = 0.15f)
                        } else {
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        },
                        labelColor = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                    ),
                    icon = {
                        Icon(
                            imageVector = if (isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (isGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        )
                    },
                )
            }

            Text(
                text = "Android requires explicit permission for Unreel to observe scroll gestures in supported apps. Look for 'Unreel Reel Detector' under Downloaded Services and toggle it ON.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp,
            )

            FilledTonalButton(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isGranted) "Modify Settings" else "Open Accessibility Settings")
            }
        }
    }
}

@Composable
fun ShieldingSetupCard(
    status: PermissionStatus,
    onGrantOverlay: () -> Unit,
    onRequestNotification: () -> Unit,
    onRequestIgnoreBattery: () -> Unit,
    onOpenAutostart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Overlay Permission
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "1. Overlay Shield",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (status.isOverlayGranted) "Granted" else "Action Needed",
                        color = if (status.isOverlayGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Allows displaying mindful pause overlays when you hit limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onGrantOverlay,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (status.isOverlayGranted) "Review Overlay Permission" else "Grant Draw Over Apps")
                }
            }
        }

        // Notifications Permission
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "2. Nudge Notifications",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = if (status.isNotificationGranted) "Enabled" else "Disabled",
                        color = if (status.isNotificationGranted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sends gentle alerts when approaching your daily or session limits.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onRequestNotification,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (status.isNotificationGranted) "Notification Settings" else "Enable Notifications")
                }
            }
        }

        // Battery Optimization and OEM Survival
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
            shape = RoundedCornerShape(14.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "3. Background Survival",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = if (status.isBatteryOptimizationIgnored) "Optimized" else "Restricted",
                        color = if (status.isBatteryOptimizationIgnored) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keeps the tracking service active and prevents Android from killing it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                FilledTonalButton(
                    onClick = onRequestIgnoreBattery,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Disable Battery Restrictions")
                }

                if (status.isAggressiveOem) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onOpenAutostart,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Configure ${status.deviceManufacturer} Autostart")
                    }
                }
            }
        }
    }
}
