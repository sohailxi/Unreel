package app.unreel.domain.usecase

import app.unreel.domain.engine.RuleEngine
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.Decision
import app.unreel.time.AppClock
import app.unreel.time.DayBoundary

/**
 * Evaluates current usage against configured limit rules for a given app.
 */
class EvaluateUsageUseCase(
    private val usageRepository: UsageRepository,
    private val ruleRepository: RuleRepository,
    private val settingsRepository: SettingsRepository,
    private val ruleEngine: RuleEngine,
    private val clock: AppClock,
) {
    suspend operator fun invoke(appId: String, bypassPassesUsed: Int = 0): Decision {
        val now = clock.now()
        val timeZone = clock.timeZone()
        val dayStartHour = settingsRepository.getDayStartHour()
        val dayBoundary = DayBoundary(dayStartHour = dayStartHour)
        val today = dayBoundary.toLocalDate(now, timeZone)

        val dailyUsage = usageRepository.getDailyUsage(today, appId)
        val activeSession = usageRepository.getActiveSession(appId)
        val rules = ruleRepository.getRulesForApp(appId)

        return ruleEngine.evaluate(
            appId = appId,
            currentTime = now,
            timeZone = timeZone,
            dailyUsage = dailyUsage,
            currentSession = activeSession,
            rules = rules,
            bypassPassesUsed = bypassPassesUsed,
        )
    }
}
