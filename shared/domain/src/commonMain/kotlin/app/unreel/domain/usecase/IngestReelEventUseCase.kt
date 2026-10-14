package app.unreel.domain.usecase

import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.DailyUsage
import app.unreel.model.Decision
import app.unreel.model.ReelEvent
import app.unreel.model.Session
import app.unreel.time.AppClock
import app.unreel.time.DayBoundary

/**
 * Use case for processing and ingesting an exact [ReelEvent] (Android).
 *
 * Flow:
 * 1. Checks if app is tracked
 * 2. Records raw immutable event (event sourcing)
 * 3. Updates active session (or creates new session on timeout / start)
 * 4. Updates daily usage aggregates
 * 5. Evaluates usage limits via [EvaluateUsageUseCase]
 * 6. Triggers platform enforcer or notifications
 * 7. Publishes session update to [LiveSurfacePublisher]
 */
class IngestReelEventUseCase(
    private val usageRepository: UsageRepository,
    private val settingsRepository: SettingsRepository,
    private val ruleRepository: RuleRepository,
    private val evaluateUsageUseCase: EvaluateUsageUseCase,
    private val enforcer: Enforcer,
    private val liveSurfacePublisher: LiveSurfacePublisher,
    private val notificationPort: NotificationPort,
    private val clock: AppClock,
    private val sessionTimeoutMs: Long = 300_000L, // 5 minutes
) {
    suspend operator fun invoke(event: ReelEvent): Decision {
        if (!settingsRepository.isAppTracked(event.appId)) {
            return Decision.Allow
        }

        // 1. Record immutable raw event
        usageRepository.recordReelEvent(event)

        // 2. Manage active session
        val activeSession = usageRepository.getActiveSession(event.appId)
        val isContinuation = activeSession != null &&
            (event.tsUtc.toEpochMilliseconds() - activeSession.endUtc.toEpochMilliseconds()) <= sessionTimeoutMs

        val session: Session = if (isContinuation) {
            checkNotNull(activeSession).copy(
                endUtc = event.tsUtc,
                reelCount = activeSession.reelCount + 1,
                durationMs = activeSession.durationMs + event.dwellMs,
            )
        } else {
            if (activeSession != null) {
                usageRepository.endSession(activeSession.id)
            }
            Session(
                id = "session-${event.appId}-${event.tsUtc.toEpochMilliseconds()}",
                appId = event.appId,
                startUtc = event.tsUtc,
                endUtc = event.tsUtc,
                reelCount = 1,
                durationMs = event.dwellMs,
                source = event.source,
            )
        }
        usageRepository.saveSession(session)

        // 3. Update daily aggregate
        val dayStartHour = settingsRepository.getDayStartHour()
        val dayBoundary = DayBoundary(dayStartHour = dayStartHour)
        val today = dayBoundary.toLocalDate(event.tsUtc, clock.timeZone())

        val currentDaily = usageRepository.getDailyUsage(today, event.appId)
        val updatedDaily: DailyUsage = if (currentDaily != null) {
            currentDaily.copy(
                reels = currentDaily.reels + 1,
                durationMs = currentDaily.durationMs + event.dwellMs,
                sessions = currentDaily.sessions + if (isContinuation) 0 else 1,
            )
        } else {
            DailyUsage(
                localDate = today,
                appId = event.appId,
                reels = 1,
                durationMs = event.dwellMs,
                sessions = 1,
                opens = 1,
                source = event.source,
            )
        }
        usageRepository.saveDailyUsage(updatedDaily)

        // 4. Evaluate limit decision
        val decision = evaluateUsageUseCase(event.appId)

        // 5. Enforce decision
        when (decision) {
            is Decision.Block -> {
                enforcer.enforce(decision)
                notificationPort.showLimitReached(title = "Limit Reached", body = decision.reason)
            }
            is Decision.Nudge -> {
                notificationPort.showNudge(title = "Heads up", body = decision.reason)
            }
            is Decision.Friction -> {
                enforcer.enforce(decision)
            }
            is Decision.Allow -> {
                enforcer.clearEnforcement()
            }
        }

        // 6. Calculate limit progress if rule is configured
        val rules = ruleRepository.getRulesForApp(event.appId)
        val limitReels = rules.firstOrNull { it.enabled && it.params.limitReels != null }?.params?.limitReels
        val limitProgress: Float? = limitReels?.let { limit ->
            (updatedDaily.reels.toFloat() / limit).coerceIn(0f, 1f)
        }

        // 7. Publish to live surface
        liveSurfacePublisher.publishSessionUpdate(
            sessionReels = session.reelCount,
            todayReels = updatedDaily.reels,
            todayDurationMs = updatedDaily.durationMs,
            limitProgress = limitProgress,
        )

        return decision
    }
}
