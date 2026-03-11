package com.aegisnet.firewall.engine

import com.aegisnet.database.dao.AppDNSRuleDao
import com.aegisnet.database.dao.AppDomainRuleDao
import com.aegisnet.database.dao.AppRoutingRuleDao
import com.aegisnet.database.entity.AppRoutingRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRuleManager @Inject constructor(
    private val appDomainRuleDao: AppDomainRuleDao,
    private val appDnsRuleDao: AppDNSRuleDao,
    private val appRoutingRuleDao: AppRoutingRuleDao
) {
    // In-memory caches for fast lookup (<1ms requirement)
    private val routingRules = mutableMapOf<Int, AppRoutingRule>()
    private val dnsRules = mutableMapOf<Int, String>()
    
    // Per-app domain matchers
    private val domainMatchers = mutableMapOf<Int, DomainRuleMatcher>()
    
    init {
        // Load all rules into memory on startup
        CoroutineScope(Dispatchers.IO).launch {
            loadRules()
        }
    }
    
    private suspend fun loadRules() {
        // Load Routing Rules
        appRoutingRuleDao.getAllRules().firstOrNull()?.forEach { rule ->
            routingRules[rule.appUid] = rule
        }
        
        // Load DNS Rules
        appDnsRuleDao.getAllRules().firstOrNull()?.forEach { rule ->
            dnsRules[rule.appUid] = rule.dnsServer
        }
        
        // Load Domain Rules
        val allDomainRules = appDomainRuleDao.getAllRules().firstOrNull() ?: emptyList()
        val rulesByApp = allDomainRules.groupBy { it.appUid }
        
        rulesByApp.forEach { (uid, rules) ->
            val matcher = DomainRuleMatcher()
            rules.forEach { rule ->
                matcher.addRule(rule.domain, rule.action, rule.matchType)
            }
            matcher.build()
            domainMatchers[uid] = matcher
        }
    }
    
    fun getRoutingRule(uid: Int): AppRoutingRule? {
        return routingRules[uid]
    }
    
    fun getAllRoutingRules(): List<AppRoutingRule> {
        return routingRules.values.toList()
    }
    
    fun getCustomDns(uid: Int): String? {
        return dnsRules[uid]
    }
    
    /**
     * Returns "BLOCK", "ALLOW", or null (no match) for a given UID and domain.
     */
    fun matchDomain(uid: Int, domain: String): String? {
        val matcher = domainMatchers[uid] ?: return null
        return matcher.match(domain)
    }
    
    // TODO: Add functions to dynamically update memory state and DB when rules change from UI
}
