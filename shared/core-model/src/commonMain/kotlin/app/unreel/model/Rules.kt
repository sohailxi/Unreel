package app.unreel.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A limit rule that governs usage enforcement.
 */
@Serializable
data class LimitRule(
    val id: String,
    val appIds: List<String>,
    val kind: RuleKind,
    val params: RuleParams,
    val schedule: Schedule?,
    val strictness: Strictness,
    val enabled: Boolean,
)

/**
 * The kind of limit rule, per Section 6.6.
 */
@Serializable
enum class RuleKind {
    DAILY_TIME_LIMIT,
    DAILY_REEL_LIMIT,
    SESSION_CAP,
    SCHEDULE_WINDOW,
    OPEN_COUNT_LIMIT,
    SHARED_BUDGET,
}

/**
 * Parameters for a limit rule. Extensible via [extra] for future rule kinds.
 */
@Serializable
data class RuleParams(
    val limitMinutes: Int? = null,
    val limitReels: Int? = null,
    val cooldownMinutes: Int? = null,
    val maxPasses: Int? = null,
    val maxOpens: Int? = null,
    val extra: JsonObject? = null,
)

/**
 * Schedule for when a rule is active (e.g., block 22:00–07:00 on weekdays).
 */
@Serializable
data class Schedule(
    val startTime: String, // HH:mm format
    val endTime: String,   // HH:mm format
    val daysOfWeek: List<Int>? = null, // 1=Monday, 7=Sunday; null = every day
)

/**
 * Strictness ladder per rule, from gentle nudge to hard block.
 */
@Serializable
enum class Strictness {
    NUDGE,
    FRICTION,
    SOFT_BLOCK,
    HARD_BLOCK,
    STRICT,
}

/**
 * The result of the rule engine evaluating current usage against active rules.
 * Sealed hierarchy: Allow, Nudge, Friction, Block.
 */
@Serializable
sealed interface Decision {
    @Serializable
    data object Allow : Decision

    @Serializable
    data class Nudge(
        val reason: String,
        val ruleId: String,
    ) : Decision

    @Serializable
    data class Friction(
        val kind: FrictionKind,
        val reason: String,
        val ruleId: String,
    ) : Decision

    @Serializable
    data class Block(
        val until: Instant?,
        val reason: String,
        val ruleId: String,
        val canBypass: Boolean,
        val passesRemaining: Int,
    ) : Decision
}

@Serializable
enum class FrictionKind {
    BREATHING_PAUSE,
    WHY_PROMPT,
}
