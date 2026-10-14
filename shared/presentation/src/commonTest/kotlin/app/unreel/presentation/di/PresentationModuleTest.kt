package app.unreel.presentation.di

import app.unreel.domain.engine.MetricsEngine
import app.unreel.domain.engine.RuleEngine
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.domain.usecase.EvaluateUsageUseCase
import app.unreel.presentation.home.HomeViewModel
import app.unreel.presentation.rules.RulesViewModel
import app.unreel.presentation.stats.StatsViewModel
import app.unreel.testing.FakeClock
import app.unreel.testing.InMemoryRuleRepository
import app.unreel.testing.InMemorySettingsRepository
import app.unreel.testing.InMemoryUsageRepository
import app.unreel.time.AppClock
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

class PresentationModuleTest {

    private var koinApp: KoinApplication? = null

    @BeforeTest
    fun setUp() {
        stopKoin()
        val testDependenciesModule = module {
            single<UsageRepository> { InMemoryUsageRepository() }
            single<RuleRepository> { InMemoryRuleRepository() }
            single<SettingsRepository> { InMemorySettingsRepository() }
            single<AppClock> { FakeClock() }
        }

        koinApp = startKoin {
            modules(testDependenciesModule, presentationModule)
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        koinApp = null
    }

    @Test
    fun resolvesAllPresentationDependencies() {
        val koin = koinApp!!.koin

        val ruleEngine: RuleEngine = koin.get()
        assertNotNull(ruleEngine)

        val metricsEngine: MetricsEngine = koin.get()
        assertNotNull(metricsEngine)

        val evaluateUsageUseCase: EvaluateUsageUseCase = koin.get()
        assertNotNull(evaluateUsageUseCase)

        val homeViewModel: HomeViewModel = koin.get()
        assertNotNull(homeViewModel)

        val statsViewModel: StatsViewModel = koin.get()
        assertNotNull(statsViewModel)

        val rulesViewModel: RulesViewModel = koin.get()
        assertNotNull(rulesViewModel)
    }
}
