package com.aegisnet.filter.engine

import com.aegisnet.filter.cache.LRUCache
import com.aegisnet.filter.store.RuleStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterEngine @Inject constructor() {
    private val blockStore = RuleStore()
    private val whitelistStore = RuleStore()
    // 10,000 capacity limits memory constraint to < 80MB peak threshold requirement
    private val cache = LRUCache<String, Boolean>(10000)
    
    val matcher = RuleMatcher(blockStore, whitelistStore, cache)

    @Synchronized
    fun loadRules(blockRules: List<UnifiedRule>, whitelistRules: List<UnifiedRule>) {
        // Clear indexes and caches
        blockStore.clear()
        whitelistStore.clear()
        cache.clear()

        // Distribute UnifiedRules into memory structures
        blockRules.forEach { blockStore.addRule(it) }
        whitelistRules.forEach { whitelistStore.addRule(it) }

        // Commit Aho-Corasick indexes
        blockStore.commit()
        whitelistStore.commit()
    }

    /**
     * Re-loads a single list append without clearing the entire cache if necessary.
     */
    @Synchronized
    fun loadBlockList(rules: List<UnifiedRule>) {
        rules.forEach { blockStore.addRule(it) }
        blockStore.commit()
        cache.clear()
    }
}
