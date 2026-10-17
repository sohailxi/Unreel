package app.unreel.testing

import app.unreel.domain.repository.RuleRepository
import app.unreel.domain.repository.SettingsRepository
import app.unreel.domain.repository.UsageRepository
import app.unreel.model.DailyUsage
import app.unreel.model.LimitRule
import app.unreel.model.ReelEvent
import app.unreel.model.Session
import app.unreel.model.UsageTick
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

/**
 * In-memory test double for [UsageRepository].
 */
class InMemoryUsageRepository : UsageRepository {
    val recordedEvents = mutableListOf<ReelEvent>()
    val recordedTicks = mutableListOf<UsageTick>()

    private val dailyUsageMap = mutableMapOf<Pair<LocalDate, String>, DailyUsage>()
    private val _dailyUsageFlow = MutableStateFlow<Map<Pair<LocalDate, String>, DailyUsage>>(emptyMap())

    private val activeSessionsMap = mutableMapOf<String, Session>()
    private val recentSessionsList = mutableListOf<Session>()

    override suspend fun recordReelEvent(event: ReelEvent) {
        recordedEvents.add(event)
    }

    override suspend fun recordUsageTick(tick: UsageTick) {
        recordedTicks.add(tick)
    }

    override suspend fun getDailyUsage(date: LocalDate, appId: String?): DailyUsage? {
        return if (appId != null) {
            dailyUsageMap[date to appId]
        } else {
            val matching = dailyUsageMap.values.filter { it.localDate == date }
            if (matching.isEmpty()) {
                null
            } else {
                DailyUsage(
                    localDate = date,
                    appId = "ALL",
                    reels = matching.sumOf { it.reels },
                    durationMs = matching.sumOf { it.durationMs },
                    sessions = matching.sumOf { it.sessions },
                    opens = matching.sumOf { it.opens },
                    source = matching.firstOrNull()?.source ?: app.unreel.model.DataSource.EXACT,
                )
            }
        }
    }

    override fun observeDailyUsage(date: LocalDate, appId: String?): Flow<DailyUsage?> {
        return _dailyUsageFlow.map { map ->
            if (appId != null) {
                map[date to appId]
            } else {
                val matching = map.values.filter { it.localDate == date }
                if (matching.isEmpty()) {
                    null
                } else {
                    DailyUsage(
                        localDate = date,
                        appId = "ALL",
                        reels = matching.sumOf { it.reels },
                        durationMs = matching.sumOf { it.durationMs },
                        sessions = matching.sumOf { it.sessions },
                        opens = matching.sumOf { it.opens },
                        source = matching.firstOrNull()?.source ?: app.unreel.model.DataSource.EXACT,
                    )
                }
            }
        }
    }

    override suspend fun getDailyUsageBreakdown(date: LocalDate): List<DailyUsage> {
        return dailyUsageMap.values
            .filter { it.localDate == date }
            .sortedByDescending { it.reels }
    }

    override fun observeDailyUsageBreakdown(date: LocalDate): Flow<List<DailyUsage>> {
        return _dailyUsageFlow.map { map ->
            map.values
                .filter { it.localDate == date }
                .sortedByDescending { it.reels }
        }
    }

    override suspend fun saveDailyUsage(dailyUsage: DailyUsage) {
        dailyUsageMap[dailyUsage.localDate to dailyUsage.appId] = dailyUsage
        _dailyUsageFlow.value = dailyUsageMap.toMap()
    }

    override suspend fun getDailyUsageHistory(
        from: LocalDate,
        to: LocalDate,
        appId: String?,
    ): List<DailyUsage> {
        return dailyUsageMap.values.filter { usage ->
            usage.localDate in from..to && (appId == null || usage.appId == appId)
        }.sortedBy { it.localDate }
    }

    override suspend fun getUsageBreakdownByApp(from: LocalDate, to: LocalDate): List<DailyUsage> {
        return dailyUsageMap.values
            .filter { it.localDate in from..to }
            .groupBy { it.appId }
            .map { (appId, usages) ->
                DailyUsage(
                    localDate = from,
                    appId = appId,
                    reels = usages.sumOf { it.reels },
                    durationMs = usages.sumOf { it.durationMs },
                    sessions = usages.sumOf { it.sessions },
                    opens = usages.sumOf { it.opens },
                    source = usages.firstOrNull()?.source ?: app.unreel.model.DataSource.EXACT,
                )
            }
            .sortedByDescending { it.reels }
    }

    override suspend fun getActiveSession(appId: String): Session? {
        return activeSessionsMap[appId]
    }

    override suspend fun saveSession(session: Session) {
        activeSessionsMap[session.appId] = session
        val index = recentSessionsList.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            recentSessionsList[index] = session
        } else {
            recentSessionsList.add(session)
        }
    }

    override suspend fun endSession(sessionId: String) {
        val entry = activeSessionsMap.entries.firstOrNull { it.value.id == sessionId }
        if (entry != null) {
            activeSessionsMap.remove(entry.key)
        }
    }

    override suspend fun getRecentSessions(limit: Int): List<Session> {
        return recentSessionsList.takeLast(limit).reversed()
    }
}

/**
 * In-memory test double for [RuleRepository].
 */
class InMemoryRuleRepository : RuleRepository {
    private val rulesList = mutableListOf<LimitRule>()
    private val _rulesFlow = MutableStateFlow<List<LimitRule>>(emptyList())

    override suspend fun getRules(): List<LimitRule> = rulesList.toList()

    override suspend fun getRulesForApp(appId: String): List<LimitRule> {
        return rulesList.filter { it.appIds.isEmpty() || appId in it.appIds }
    }

    override fun observeRules(): Flow<List<LimitRule>> = _rulesFlow.asStateFlow()

    override suspend fun saveRule(rule: LimitRule) {
        val index = rulesList.indexOfFirst { it.id == rule.id }
        if (index >= 0) {
            rulesList[index] = rule
        } else {
            rulesList.add(rule)
        }
        _rulesFlow.value = rulesList.toList()
    }

    override suspend fun deleteRule(ruleId: String) {
        rulesList.removeAll { it.id == ruleId }
        _rulesFlow.value = rulesList.toList()
    }
}

/**
 * In-memory test double for [SettingsRepository].
 */
class InMemorySettingsRepository(
    initialDayStartHour: Int = 4,
    initialFocusGoalReels: Int = 50,
    initialFocusGoalMinutes: Int = 30,
) : SettingsRepository {
    private val _dayStartHour = MutableStateFlow(initialDayStartHour)
    private val _focusGoalReels = MutableStateFlow(initialFocusGoalReels)
    private val _focusGoalMinutes = MutableStateFlow(initialFocusGoalMinutes)
    private val trackedAppsMap = mutableMapOf<String, Boolean>()

    override suspend fun getDayStartHour(): Int = _dayStartHour.value

    override suspend fun setDayStartHour(hour: Int) {
        _dayStartHour.value = hour
    }

    override fun observeDayStartHour(): Flow<Int> = _dayStartHour.asStateFlow()

    override suspend fun getFocusGoalReels(): Int = _focusGoalReels.value

    override suspend fun setFocusGoalReels(reels: Int) {
        _focusGoalReels.value = reels
    }

    override fun observeFocusGoalReels(): Flow<Int> = _focusGoalReels.asStateFlow()

    override suspend fun getFocusGoalMinutes(): Int = _focusGoalMinutes.value

    override suspend fun setFocusGoalMinutes(minutes: Int) {
        _focusGoalMinutes.value = minutes
    }

    override fun observeFocusGoalMinutes(): Flow<Int> = _focusGoalMinutes.asStateFlow()

    override suspend fun isAppTracked(appId: String): Boolean {
        return trackedAppsMap[appId] ?: true
    }

    override suspend fun setAppTracked(appId: String, tracked: Boolean) {
        trackedAppsMap[appId] = tracked
    }
}
