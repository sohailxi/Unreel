package app.unreel.android.di

import android.content.Context
import app.unreel.android.platform.AndroidLiveSurfacePublisher
import app.unreel.android.platform.AndroidNotificationPort
import app.unreel.android.platform.AndroidOverlayEnforcer
import app.unreel.data.driver.AndroidDatabaseDriverFactory
import app.unreel.data.driver.DatabaseDriverFactory
import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.usecase.IngestReelEventUseCase
import app.unreel.domain.usecase.IngestUsageTickUseCase
import app.unreel.time.AppClock
import app.unreel.time.SystemAppClock
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module providing Android platform implementations for ports and use cases.
 */
val androidAppModule: Module = module {
    single<AppClock> { SystemAppClock() }

    single<DatabaseDriverFactory> {
        AndroidDatabaseDriverFactory(context = androidContext())
    }

    single<Settings> {
        val prefs = androidContext().getSharedPreferences("unreel_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(prefs)
    }

    single { AndroidOverlayEnforcer() }
    single<Enforcer> { get<AndroidOverlayEnforcer>() }

    single<NotificationPort> {
        AndroidNotificationPort(context = androidContext())
    }

    single { AndroidLiveSurfacePublisher() }
    single<LiveSurfacePublisher> { get<AndroidLiveSurfacePublisher>() }

    factory {
        IngestReelEventUseCase(
            usageRepository = get(),
            settingsRepository = get(),
            ruleRepository = get(),
            evaluateUsageUseCase = get(),
            enforcer = get(),
            liveSurfacePublisher = get(),
            notificationPort = get(),
            clock = get(),
        )
    }

    factory {
        IngestUsageTickUseCase(
            usageRepository = get(),
            settingsRepository = get(),
            ruleRepository = get(),
            evaluateUsageUseCase = get(),
            enforcer = get(),
            liveSurfacePublisher = get(),
            notificationPort = get(),
            clock = get(),
        )
    }
}
