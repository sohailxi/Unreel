package app.unreel.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.unreel.presentation.home.HomeViewModel
import app.unreel.presentation.rules.RulesViewModel
import app.unreel.presentation.stats.StatsViewModel
import com.russhwolf.settings.Settings
import org.koin.compose.koinInject

const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    HOME("Scroll", Icons.Rounded.PlayCircle, Icons.Outlined.PlayCircleOutline),
    STATS("Trends", Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    RULES("Limits", Icons.Rounded.Shield, Icons.Outlined.Shield),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnreelApp(
    homeViewModel: HomeViewModel = koinInject(),
    statsViewModel: StatsViewModel = koinInject(),
    rulesViewModel: RulesViewModel = koinInject(),
    settings: Settings = koinInject(),
    modifier: Modifier = Modifier,
) {
    var isOnboardingCompleted by remember {
        mutableStateOf(settings.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false))
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onComplete = {
                settings.putBoolean(KEY_HAS_COMPLETED_ONBOARDING, true)
                isOnboardingCompleted = true
            },
        )
        return
    }

    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.MovieFilter,
                            contentDescription = "Unreel Logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Unreel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Reclaim your time",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isOnboardingCompleted = false },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = "Setup Guide & Privacy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                            )
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen(viewModel = homeViewModel)
                AppTab.STATS -> StatsScreen(viewModel = statsViewModel)
                AppTab.RULES -> RulesScreen(viewModel = rulesViewModel)
            }
        }
    }
}
