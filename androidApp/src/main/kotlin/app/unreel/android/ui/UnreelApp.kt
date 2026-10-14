package app.unreel.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import app.unreel.presentation.home.HomeViewModel
import app.unreel.presentation.rules.RulesViewModel
import app.unreel.presentation.stats.StatsViewModel
import org.koin.compose.koinInject

enum class AppTab(val title: String) {
    HOME("Scroll"),
    STATS("Trends"),
    RULES("Limits"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnreelApp(
    homeViewModel: HomeViewModel = koinInject(),
    statsViewModel: StatsViewModel = koinInject(),
    rulesViewModel: RulesViewModel = koinInject(),
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Unreel",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.title) },
                        icon = {
                            Text(
                                text = when (tab) {
                                    AppTab.HOME -> "📱"
                                    AppTab.STATS -> "📈"
                                    AppTab.RULES -> "🛡️"
                                },
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
