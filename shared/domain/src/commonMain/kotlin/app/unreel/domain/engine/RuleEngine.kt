package app.unreel.domain.engine

import app.unreel.model.DailyUsage
import app.unreel.model.Decision
import app.unreel.model.FrictionKind
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.Schedule
import app.unreel.model.Session
import app.unreel.model.Strictness
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Evaluates current short-video usage against active limit rules and schedules,
 * resolving to the strictest appropriate [Decision].
 */
class RuleEngine {

    /**
     * Evaluates all enabled rules for [appId] at [currentTime] and returns the strictest [Decision].
     */
    fun evaluate(
        appId: String,
        currentTime: Instant,
        timeZone: TimeZone,
        dailyUsage: DailyUsage?,
        currentSession: Session?,
        rules: List<LimitRule>,
        bypassPassesUsed: Int = 0,
    ): Decision {
        val matchingRules = rules.filter { it.enabled && (it.appIds.isEmpty() || appId in it.appIds) }
        if (matchingRules.isEmpty()) return Decision.Allow

        val triggeredDecisions = mutableListOf<Decision>()

        for (rule in matchingRules) {
            val triggered = isRuleTriggered(rule, currentTime, timeZone, dailyUsage, currentSession)
            if (triggered) {
                val decision = buildDecision(rule, bypassPassesUsed)
                triggeredDecisions.add(decision)
            }
        }

        return selectStrictestDecision(triggeredDecisions)
    }

    private fun isRuleTriggered(
        rule: LimitRule,
        currentTime: Instant,
        timeZone: TimeZone,
        dailyUsage: DailyUsage?,
        currentSession: Session?,
    ): Boolean {
        val schedule = rule.schedule
        if (schedule != null && !isScheduleActive(schedule, currentTime, timeZone)) {
            return false
        }

        return when (rule.kind) {
            RuleKind.DAILY_REEL_LIMIT -> {
                val limit = rule.params.limitReels ?: return false
                val current = dailyUsage?.reels ?: 0
                current >= limit
            }
            RuleKind.DAILY_TIME_LIMIT -> {
                val limitMinutes = rule.params.limitMinutes ?: return false
                val currentMinutes = (dailyUsage?.durationMs ?: 0L) / 60_000L
                currentMinutes >= limitMinutes
            }
            RuleKind.SESSION_CAP -> {
                val limitMinutes = rule.params.limitMinutes
                val limitReels = rule.params.limitReels
                val sessionMs = currentSession?.durationMs ?: 0L
                val sessionReels = currentSession?.reelCount ?: 0

                val timeExceeded = limitMinutes != null && (sessionMs / 60_000L) >= limitMinutes
                val reelsExceeded = limitReels != null && sessionReels >= limitReels
                timeExceeded || reelsExceeded
            }
            RuleKind.SCHEDULE_WINDOW -> {
                rule.schedule != null
            }
            RuleKind.OPEN_COUNT_LIMIT -> {
                val maxOpens = rule.params.maxOpens ?: return false
                val opens = dailyUsage?.opens ?: 0
                opens >= maxOpens
            }
            RuleKind.SHARED_BUDGET -> {
                val limitMinutes = rule.params.limitMinutes ?: return false
                val currentMinutes = (dailyUsage?.durationMs ?: 0L) / 60_000L
                currentMinutes >= limitMinutes
            }
        }
    }

    private fun isScheduleActive(
        schedule: Schedule,
        currentTime: Instant,
        timeZone: TimeZone,
    ): Boolean {
        val ldt = currentTime.toLocalDateTime(timeZone)

        val daysOfWeek = schedule.daysOfWeek
        if (daysOfWeek != null && daysOfWeek.isNotEmpty()) {
            val dayOfWeek = ldt.dayOfWeek.ordinal + 1 // 1 = Monday, 7 = Sunday
            if (dayOfWeek !in daysOfWeek) {
                return false
            }
        }

        val currentLocalTime = ldt.time
        val start = parseTime(schedule.startTime)
        val end = parseTime(schedule.endTime)

        return if (start <= end) {
            currentLocalTime in start..end
        } else {
            // Overnight window (e.g. 22:00 to 07:00)
            currentLocalTime >= start || currentLocalTime <= end
        }
    }

    private fun parseTime(timeStr: String): LocalTime {
        val parts = timeStr.trim().split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return LocalTime(hour, minute)
    }

    private fun buildDecision(rule: LimitRule, bypassPassesUsed: Int): Decision {
        val reason = "Limit reached for ${rule.kind.name.lowercase().replace('_', ' ')}"
        return when (rule.strictness) {
            Strictness.NUDGE -> Decision.Nudge(
                reason = reason,
                ruleId = rule.id,
            )
            Strictness.FRICTION -> Decision.Friction(
                kind = FrictionKind.BREATHING_PAUSE,
                reason = reason,
                ruleId = rule.id,
            )
            Strictness.SOFT_BLOCK -> {
                val maxPasses = rule.params.maxPasses ?: 1
                val remaining = (maxPasses - bypassPassesUsed).coerceAtLeast(0)
                Decision.Block(
                    until = null,
                    reason = reason,
                    ruleId = rule.id,
                    canBypass = remaining > 0,
                    passesRemaining = remaining,
                )
            }
            Strictness.HARD_BLOCK, Strictness.STRICT -> Decision.Block(
                until = null,
                reason = reason,
                ruleId = rule.id,
                canBypass = false,
                passesRemaining = 0,
            )
        }
    }

    private fun selectStrictestDecision(decisions: List<Decision>): Decision {
        if (decisions.isEmpty()) return Decision.Allow
        return decisions.maxByOrNull { decisionRank(it) } ?: Decision.Allow
    }

    private fun decisionRank(decision: Decision): Int {
        return when (decision) {
            is Decision.Allow -> 0
            is Decision.Nudge -> 1
            is Decision.Friction -> 2
            is Decision.Block -> if (decision.canBypass) 3 else 4
        }
    }
}
