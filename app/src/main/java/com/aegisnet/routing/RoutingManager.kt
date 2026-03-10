package com.aegisnet.routing

import com.aegisnet.database.dao.RoutingRuleDao
import com.aegisnet.database.entity.RoutingRule
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmartRoutingEngine @Inject constructor(
    private val routingRuleDao: RoutingRuleDao
) {
    suspend fun getActiveRules(): List<RoutingRule> {
        return routingRuleDao.getAll().first().filter { it.isEnabled }
    }

    // Dynamic checks could be performed here, but sing-box JSON configuration
    // (handled by SingboxConfigBuilder) inherently processes GeoIP, Domain matching natively.
    // This engine acts as the intermediary to provide those configurations.
}
