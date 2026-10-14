package app.unreel.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.unreel.data.db.Limit_rule_record
import app.unreel.data.db.UnreelDatabase
import app.unreel.domain.repository.RuleRepository
import app.unreel.model.LimitRule
import app.unreel.model.RuleKind
import app.unreel.model.RuleParams
import app.unreel.model.Schedule
import app.unreel.model.Strictness
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * SQLDelight-backed implementation of [RuleRepository].
 * Serializes extensible [RuleParams] and [Schedule] as JSON payloads.
 */
class SqlDelightRuleRepository(
    private val db: UnreelDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : RuleRepository {

    override suspend fun getRules(): List<LimitRule> = withContext(ioDispatcher) {
        db.limitRuleQueries.selectAllRules()
            .executeAsList()
            .map { it.toDomain() }
    }

    override suspend fun getRulesForApp(appId: String): List<LimitRule> = withContext(ioDispatcher) {
        getRules().filter { rule ->
            rule.appIds.isEmpty() || appId in rule.appIds
        }
    }

    override fun observeRules(): Flow<List<LimitRule>> {
        return db.limitRuleQueries.selectAllRules()
            .asFlow()
            .mapToList(ioDispatcher)
            .map { records -> records.map { it.toDomain() } }
    }

    override suspend fun saveRule(rule: LimitRule) = withContext(ioDispatcher) {
        db.limitRuleQueries.insertOrReplaceRule(
            id = rule.id,
            appIdsJson = json.encodeToString(rule.appIds),
            kind = rule.kind.name,
            paramsJson = json.encodeToString(rule.params),
            scheduleJson = rule.schedule?.let { json.encodeToString(it) },
            strictness = rule.strictness.name,
            enabled = if (rule.enabled) 1L else 0L,
        )
    }

    override suspend fun deleteRule(ruleId: String) = withContext(ioDispatcher) {
        db.limitRuleQueries.deleteRuleById(ruleId)
    }

    private fun Limit_rule_record.toDomain(): LimitRule = LimitRule(
        id = id,
        appIds = json.decodeFromString<List<String>>(app_ids_json),
        kind = RuleKind.valueOf(kind),
        params = json.decodeFromString<RuleParams>(params_json),
        schedule = schedule_json?.let { json.decodeFromString<Schedule>(it) },
        strictness = Strictness.valueOf(strictness),
        enabled = enabled == 1L,
    )
}
