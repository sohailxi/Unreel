package app.unreel.android.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.unreel.android.platform.PermissionManager
import org.koin.compose.koinInject

/**
 * First-run 3-step onboarding carousel guiding users through privacy principles,
 * Accessibility service reel tracking, and Overlay shielding / OEM survival permissions.
 */
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    permissionManager: PermissionManager = koinInject(),
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var permissionStatus by remember { mutableStateOf(permissionManager.getPermissionStatus()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
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

    val steps = OnboardingStep.entries
    val currentStep = steps[currentStepIndex]
    val isLast = isLastStep(currentStepIndex, steps.size)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (index == currentStepIndex) 28.dp else 12.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (index == currentStepIndex) {
                                        MaterialTheme.colorScheme.primary
                                    } else if (index < currentStepIndex) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                ),
                        )
                    }
                }

                if (!isLast) {
                    TextButton(onClick = onComplete) {
                        Text(
                            text = "Skip",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex = calculatePreviousStep(currentStepIndex) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (!isLast) {
                    Button(
                        onClick = {
                            currentStepIndex = calculateNextStep(currentStepIndex, steps.size)
                        },
                    ) {
                        Text("Next")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Next",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            try {
                                permissionManager.startMonitoringService()
                            } catch (_: Throwable) {
                                // Service start fallback
                            }
                            onComplete()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RocketLaunch,
                            contentDescription = "Get Started",
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Started", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "OnboardingStepTransition",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) { step ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = step.icon,
                        contentDescription = step.title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp),
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = step.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                )

                Spacer(modifier = Modifier.height(28.dp))

                when (step) {
                    OnboardingStep.WELCOME -> {
                        WelcomePrivacyFeaturesCard()
                    }

                    OnboardingStep.ACCESSIBILITY -> {
                        AccessibilitySetupCard(
                            isGranted = permissionStatus.isAccessibilityEnabled,
                            onOpenSettings = { permissionManager.openAccessibilitySettings() },
                        )
                    }

                    OnboardingStep.SHIELDING -> {
                        ShieldingSetupCard(
                            status = permissionStatus,
                            onGrantOverlay = { permissionManager.openOverlaySettings() },
                            onRequestNotification = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    permissionManager.openNotificationSettings()
                                }
                            },
                            onSendTestNotification = {
                                permissionManager.sendTestNotification()
                            },
                            onRequestIgnoreBattery = {
                                permissionManager.requestIgnoreBatteryOptimizations()
                            },
                            onOpenAutostart = {
                                permissionManager.openAutostartSettings()
                            },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
