package app.unreel.android.di

import app.unreel.android.platform.AndroidLiveSurfacePublisher
import app.unreel.android.platform.AndroidNotificationPort
import app.unreel.android.platform.AndroidOverlayEnforcer
import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.domain.usecase.IngestReelEventUseCase
import app.unreel.domain.usecase.IngestUsageTickUseCase
import app.unreel.presentation.di.presentationModule
import app.unreel.presentation.home.HomeViewModel
import app.unreel.presentation.rules.RulesViewModel
import app.unreel.presentation.stats.StatsViewModel
import app.unreel.testing.FakeClock
import app.unreel.testing.InMemoryRuleRepository
import app.unreel.testing.InMemorySettingsRepository
import app.unreel.testing.InMemoryUsageRepository
import app.unreel.time.AppClock
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class AndroidAppModuleTest : KoinTest {

    private val testOverrides = module {
        single<UsageRepository> { InMemoryUsageRepository() }
        single<RuleRepository> { InMemoryRuleRepository() }
        single<SettingsRepository> { InMemorySettingsRepository() }
        single<AppClock> { FakeClock() }
        single<NotificationPort> { AndroidNotificationPort(context = null) }
    }

    @BeforeTest
    fun setUp() {
        startKoin {
            modules(
                presentationModule,
                androidAppModule,
                testOverrides,
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `androidAppModule resolves platform enforcer, surface publisher, and use cases`() {
        val enforcer = get<Enforcer>()
        assertNotNull(enforcer)
        assertNotNull(get<AndroidOverlayEnforcer>())
        assertNotNull(get<app.unreel.android.platform.OverlayController>())

        val publisher = get<LiveSurfacePublisher>()
        assertNotNull(publisher)
        assertNotNull(get<AndroidLiveSurfacePublisher>())

        val reelUseCase = get<IngestReelEventUseCase>()
        assertNotNull(reelUseCase)

        assertNotNull(get<app.unreel.android.detector.ReelDetectorEngine>())

        val tickUseCase = get<IngestUsageTickUseCase>()
        assertNotNull(tickUseCase)

        val permissionManager = get<app.unreel.android.platform.PermissionManager>()
        assertNotNull(permissionManager)
    }

    @Test
    fun `view models resolve cleanly with presentation and android modules`() {
        val homeViewModel = get<HomeViewModel>()
        assertNotNull(homeViewModel)

        val statsViewModel = get<StatsViewModel>()
        assertNotNull(statsViewModel)

        val rulesViewModel = get<RulesViewModel>()
        assertNotNull(rulesViewModel)
    }

    @Test
    fun `androidAppModule provides AppClock without overrides`() {
        stopKoin()
        startKoin {
            modules(androidAppModule)
        }
        val clock = get<AppClock>()
        assertNotNull(clock)
    }
}
