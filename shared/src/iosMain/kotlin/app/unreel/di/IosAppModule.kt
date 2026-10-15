package app.unreel.di

import app.unreel.data.di.dataModule
import app.unreel.data.driver.DatabaseDriverFactory
import app.unreel.data.driver.IosDatabaseDriverFactory
import app.unreel.presentation.di.presentationModule
import app.unreel.presentation.home.HomeState
import app.unreel.presentation.home.HomeViewModel
import app.unreel.presentation.rules.RulesState
import app.unreel.presentation.rules.RulesViewModel
import app.unreel.presentation.stats.StatsState
import app.unreel.presentation.stats.StatsViewModel
import app.unreel.time.AppClock
import app.unreel.time.SystemAppClock
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * iOS application Koin module providing platform ports, clock, settings, and database driver.
 */
val iosAppModule: Module = module {
    single<AppClock> { SystemAppClock() }

    single<DatabaseDriverFactory> {
        IosDatabaseDriverFactory()
    }

    single<Settings> {
        val userDefaults = NSUserDefaults(suiteName = IosDatabaseDriverFactory.APP_GROUP_ID)
        NSUserDefaultsSettings(userDefaults)
    }
}

/**
 * Initializes Koin for iOS. Safe to call multiple times.
 */
fun startKoinIos() {
    try {
        startKoin {
            modules(
                dataModule,
                presentationModule,
                iosAppModule,
            )
        }
    } catch (_: Exception) {
        // Koin already started
    }
}

/**
 * Helper class for Swift to retrieve ViewModels and observe StateFlows easily.
 */
class KoinHelper : KoinComponent {
    companion object {
        fun start() = startKoinIos()
    }

    fun getHomeViewModel(): HomeViewModel = get()
    fun getStatsViewModel(): StatsViewModel = get()
    fun getRulesViewModel(): RulesViewModel = get()

    fun observeHomeState(viewModel: HomeViewModel, onEach: (HomeState) -> Unit): () -> Unit {
        val scope = CoroutineScope(Dispatchers.Main)
        val job = scope.launch {
            viewModel.state.collect { onEach(it) }
        }
        return { job.cancel() }
    }

    fun observeStatsState(viewModel: StatsViewModel, onEach: (StatsState) -> Unit): () -> Unit {
        val scope = CoroutineScope(Dispatchers.Main)
        val job = scope.launch {
            viewModel.state.collect { onEach(it) }
        }
        return { job.cancel() }
    }

    fun observeRulesState(viewModel: RulesViewModel, onEach: (RulesState) -> Unit): () -> Unit {
        val scope = CoroutineScope(Dispatchers.Main)
        val job = scope.launch {
            viewModel.state.collect { onEach(it) }
        }
        return { job.cancel() }
    }
}
