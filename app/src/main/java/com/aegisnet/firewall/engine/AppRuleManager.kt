package com.aegisnet.firewall.engine

import com.aegisnet.database.dao.AppDNSRuleDao
import com.aegisnet.database.dao.AppDomainRuleDao
import com.aegisnet.database.dao.AppRoutingRuleDao
import com.aegisnet.database.entity.AppDNSRule
import com.aegisnet.database.entity.AppDomainRule
import com.aegisnet.database.entity.AppRoutingRule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRuleManager @Inject constructor(
    private val appDomainRuleDao: AppDomainRuleDao,
    private val appDnsRuleDao: AppDNSRuleDao,
    private val appRoutingRuleDao: AppRoutingRuleDao
) {
    // ConcurrentHashMap for thread-safe access from the VPN pipeline
    private val routingRules = ConcurrentHashMap<Int, AppRoutingRule>()
    private val dnsRules = ConcurrentHashMap<Int, String>()

    // Per-app domain matchers
    private val domainMatchers = ConcurrentHashMap<Int, DomainRuleMatcher>()

    init {
        // Observe all rule tables so in-memory caches stay in sync with DB changes
        CoroutineScope(Dispatchers.IO).launch {
            loadRules()
        }
    }

    private fun loadRules() {
        // Observe Routing Rules
        CoroutineScope(Dispatchers.IO).launch {
            appRoutingRuleDao.getAllRules().collect { rules ->
                routingRules.clear()
                rules.forEach { rule -> routingRules[rule.appUid] = rule }
            }
        }

        // Observe DNS Rules
        CoroutineScope(Dispatchers.IO).launch {
            appDnsRuleDao.getAllRules().collect { rules ->
                dnsRules.clear()
                rules.forEach { rule -> dnsRules[rule.appUid] = rule.dnsServer }
            }
        }

        // Observe Domain Rules
        CoroutineScope(Dispatchers.IO).launch {
            appDomainRuleDao.getAllRules().collect { allDomainRules ->
                val newMatchers = ConcurrentHashMap<Int, DomainRuleMatcher>()
                val rulesByApp = allDomainRules.groupBy { it.appUid }
                rulesByApp.forEach { (uid, rules) ->
                    val matcher = DomainRuleMatcher()
                    rules.forEach { rule -> matcher.addRule(rule.domain, rule.action, rule.matchType) }
                    matcher.build()
                    newMatchers[uid] = matcher
                }
                domainMatchers.clear()
                domainMatchers.putAll(newMatchers)
            }
        }
    }

    // ── Read API ──────────────────────────────────────────────────────────────

    fun getRoutingRule(uid: Int): AppRoutingRule? = routingRules[uid]

    fun getAllRoutingRules(): List<AppRoutingRule> = routingRules.values.toList()

    fun getCustomDns(uid: Int): String? = dnsRules[uid]

    /**
     * Returns "BLOCK", "ALLOW", or null (no match) for a given UID and domain.
     */
    fun matchDomain(uid: Int, domain: String): String? = domainMatchers[uid]?.match(domain)

    // ── Write API (updates DB; in-memory cache is kept in sync via Flow) ──────

    suspend fun upsertRoutingRule(rule: AppRoutingRule) {
        appRoutingRuleDao.insert(rule)
    }

    suspend fun removeRoutingRule(rule: AppRoutingRule) {
        appRoutingRuleDao.delete(rule)
        routingRules.remove(rule.appUid)
    }

    suspend fun upsertDnsRule(rule: AppDNSRule) {
        appDnsRuleDao.insert(rule)
    }

    suspend fun removeDnsRule(rule: AppDNSRule) {
        appDnsRuleDao.delete(rule)
        dnsRules.remove(rule.appUid)
    }

    suspend fun upsertDomainRule(rule: AppDomainRule) {
        appDomainRuleDao.insert(rule)
    }

    suspend fun removeDomainRule(rule: AppDomainRule) {
        appDomainRuleDao.delete(rule)
        // Matcher for this app will be rebuilt by the Flow collector; eagerly remove stale entry
        domainMatchers.remove(rule.appUid)
    }
}
