package app.unreel.android.di

import android.content.Context
import app.unreel.android.platform.AndroidLiveSurfacePublisher
import app.unreel.android.platform.AndroidNotificationPort
import app.unreel.android.platform.AndroidOverlayEnforcer
import app.unreel.android.platform.AndroidScrollEventSource
import app.unreel.data.driver.AndroidDatabaseDriverFactory
import app.unreel.data.driver.DatabaseDriverFactory
import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.port.ScrollEventSource
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
 * Android application Koin module providing platform ports, enforcers, and use cases.
 */
val androidAppModule: Module = module {
    single<AppClock> { SystemAppClock() }

    single<DatabaseDriverFactory> {
        val context = runCatching { androidContext() }.getOrNull()
            ?: throw IllegalStateException("Android Context must be provided to Koin via androidContext(...)")
        AndroidDatabaseDriverFactory(context = context)
    }

    single<Settings> {
        val context = runCatching { androidContext() }.getOrNull()
            ?: throw IllegalStateException("Android Context must be provided to Koin via androidContext(...)")
        val prefs = context.getSharedPreferences("unreel_preferences", Context.MODE_PRIVATE)
        SharedPreferencesSettings(prefs)
    }

    single { AndroidOverlayEnforcer() }
    single<Enforcer> { get<AndroidOverlayEnforcer>() }

    single { AndroidLiveSurfacePublisher() }
    single<LiveSurfacePublisher> { get<AndroidLiveSurfacePublisher>() }

    single { AndroidScrollEventSource() }
    single<ScrollEventSource> { get<AndroidScrollEventSource>() }

    single<NotificationPort> { AndroidNotificationPort(context = runCatching { androidContext() }.getOrNull()) }

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
