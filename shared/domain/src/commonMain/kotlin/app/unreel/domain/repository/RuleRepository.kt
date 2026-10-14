package app.unreel.domain.repository

import app.unreel.model.LimitRule
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing limit rules.
 */
interface RuleRepository {
    suspend fun getRules(): List<LimitRule>
    suspend fun getRulesForApp(appId: String): List<LimitRule>
    fun observeRules(): Flow<List<LimitRule>>
    suspend fun saveRule(rule: LimitRule)
    suspend fun deleteRule(ruleId: String)
}
