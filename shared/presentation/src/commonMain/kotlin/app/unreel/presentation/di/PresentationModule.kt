package app.unreel.presentation.di

import app.unreel.domain.engine.MetricsEngine
import app.unreel.domain.engine.RuleEngine
import app.unreel.domain.usecase.EvaluateUsageUseCase
import app.unreel.presentation.home.HomeViewModel
import app.unreel.presentation.rules.RulesViewModel
import app.unreel.presentation.stats.StatsViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin module declaring dependencies and ViewModels for the presentation layer.
 */
val presentationModule: Module = module {
    single { RuleEngine() }
    single { MetricsEngine() }

    factory {
        EvaluateUsageUseCase(
            usageRepository = get(),
            ruleRepository = get(),
            settingsRepository = get(),
            ruleEngine = get(),
            clock = get(),
        )
    }

    factory {
        HomeViewModel(
            usageRepository = get(),
            settingsRepository = get(),
            evaluateUsageUseCase = get(),
            metricsEngine = get(),
            clock = get(),
        )
    }

    factory {
        StatsViewModel(
            usageRepository = get(),
            settingsRepository = get(),
            metricsEngine = get(),
            clock = get(),
        )
    }

    factory {
        RulesViewModel(
            ruleRepository = get(),
            settingsRepository = get(),
        )
    }
}
