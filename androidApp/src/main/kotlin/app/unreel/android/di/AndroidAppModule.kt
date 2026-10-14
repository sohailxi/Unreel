package app.unreel.android.di

import app.unreel.android.platform.AndroidLiveSurfacePublisher
import app.unreel.android.platform.AndroidNotificationPort
import app.unreel.android.platform.AndroidOverlayEnforcer
import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.usecase.IngestReelEventUseCase
import app.unreel.domain.usecase.IngestUsageTickUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android application Koin module providing platform ports, enforcers, and use cases.
 */
val androidAppModule: Module = module {
    single { AndroidOverlayEnforcer() }
    single<Enforcer> { get<AndroidOverlayEnforcer>() }

    single { AndroidLiveSurfacePublisher() }
    single<LiveSurfacePublisher> { get<AndroidLiveSurfacePublisher>() }

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
