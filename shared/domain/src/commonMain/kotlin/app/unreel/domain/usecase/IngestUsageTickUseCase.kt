package app.unreel.domain.usecase

import app.unreel.domain.port.Enforcer
import app.unreel.domain.port.LiveSurfacePublisher
import app.unreel.domain.port.NotificationPort
import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.DailyUsage
import app.unreel.model.Decision
import app.unreel.model.Session
import app.unreel.model.UsageTick
import app.unreel.time.AppClock
import app.unreel.time.DayBoundary

/**
 * Use case for processing and ingesting an estimated [UsageTick] (iOS).
 *
 * Flow:
 * 1. Checks if app is tracked
 * 2. Records raw immutable tick (event sourcing)
 * 3. Updates active session duration (or creates new session on timeout / start)
 * 4. Updates daily usage duration aggregates (marked ESTIMATED)
 * 5. Evaluates usage limits via [EvaluateUsageUseCase]
 * 6. Triggers platform enforcer or notifications
 * 7. Publishes session update to [LiveSurfacePublisher]
 */
class IngestUsageTickUseCase(
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
    suspend operator fun invoke(tick: UsageTick): Decision {
        if (!settingsRepository.isAppTracked(tick.appId)) {
            return Decision.Allow
        }

        val tickDurationMs = tick.activeSeconds * 1000L

        // 1. Record immutable raw tick
        usageRepository.recordUsageTick(tick)

        // 2. Manage active session
        val activeSession = usageRepository.getActiveSession(tick.appId)
        val isContinuation = activeSession != null &&
            (tick.tsUtc.toEpochMilliseconds() - activeSession.endUtc.toEpochMilliseconds()) <= sessionTimeoutMs

        val session: Session = if (isContinuation) {
            checkNotNull(activeSession).copy(
                endUtc = tick.tsUtc,
                durationMs = activeSession.durationMs + tickDurationMs,
            )
        } else {
            if (activeSession != null) {
                usageRepository.endSession(activeSession.id)
            }
            Session(
                id = "session-${tick.appId}-${tick.tsUtc.toEpochMilliseconds()}",
                appId = tick.appId,
                startUtc = tick.tsUtc,
                endUtc = tick.tsUtc,
                reelCount = 0,
                durationMs = tickDurationMs,
                source = tick.source,
            )
        }
        usageRepository.saveSession(session)

        // 3. Update daily aggregate
        val dayStartHour = settingsRepository.getDayStartHour()
        val dayBoundary = DayBoundary(dayStartHour = dayStartHour)
        val today = dayBoundary.toLocalDate(tick.tsUtc, clock.timeZone())

        val currentDaily = usageRepository.getDailyUsage(today, tick.appId)
        val updatedDaily: DailyUsage = if (currentDaily != null) {
            currentDaily.copy(
                durationMs = currentDaily.durationMs + tickDurationMs,
                sessions = currentDaily.sessions + if (isContinuation) 0 else 1,
            )
        } else {
            DailyUsage(
                localDate = today,
                appId = tick.appId,
                reels = 0,
                durationMs = tickDurationMs,
                sessions = 1,
                opens = 1,
                source = tick.source,
            )
        }
        usageRepository.saveDailyUsage(updatedDaily)

        // 4. Evaluate limit decision
        val decision = evaluateUsageUseCase(tick.appId)

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

        // 6. Calculate limit progress if time limit is configured
        val rules = ruleRepository.getRulesForApp(tick.appId)
        val limitMinutes = rules.firstOrNull { it.enabled && it.params.limitMinutes != null }?.params?.limitMinutes
        val limitProgress: Float? = limitMinutes?.let { limit ->
            val limitMs = limit * 60_000L
            (updatedDaily.durationMs.toFloat() / limitMs).coerceIn(0f, 1f)
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
