package com.aegisnet.filter.engine

import com.aegisnet.filter.store.RuleStore
import com.aegisnet.filter.cache.LRUCache

/**
 * High-performance pipeline executing Android-side validation evaluating whitelists constraints first.
 */
class RuleMatcher(
    private val blockStore: RuleStore,
    private val whitelistStore: RuleStore,
    private val cache: LRUCache<String, Boolean>
) {
    /**
     * Evaluates a network domain in < 1ms constraint.
     * @return TRUE if domain should be BLOCKED. FALSE if allowed.
     */
    fun isBlocked(domain: String): Boolean {
        // 1. Fast LRU Cache Bypass (~0.1ms)
        val cached = cache.get(domain)
        if (cached != null) return cached

        // 2. Check Whitelist Exceptions first (Overrides everything)
        val isWhitelisted = whitelistStore.matchDomain(domain)
        if (isWhitelisted != null) {
            cache.put(domain, false)
            return false
        }

        // 3. Check Block Store (Exact -> Suffix -> Keyword)
        val isBlocked = blockStore.matchDomain(domain)
        if (isBlocked != null) {
            // Exception check (like @@ overrides within same list)
            if (isBlocked.second) {
                cache.put(domain, false)
                return false
            }
            cache.put(domain, true)
            return true
        }

        // Default Allowed
        cache.put(domain, false)
        return false
    }
}
