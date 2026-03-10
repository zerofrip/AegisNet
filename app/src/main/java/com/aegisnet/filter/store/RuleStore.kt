package com.aegisnet.filter.store

import com.aegisnet.filter.engine.RuleType
import com.aegisnet.filter.engine.UnifiedRule

/**
 * Wraps individual memory storage trees managing >200,000 UnifiedRules synchronously.
 */
class RuleStore {
    private val exactMap = ExactDomainMap()
    private val suffixTrie = DomainSuffixTrie()
    private val keywordMatcher = KeywordMatcher()
    private val urlMatcher = URLMatcher()

    fun addRule(rule: UnifiedRule) {
        when (rule.type) {
            RuleType.EXACT_DOMAIN -> exactMap.insert(rule.value, rule.exception)
            RuleType.DOMAIN_SUFFIX -> suffixTrie.insert(rule.value, rule.exception)
            RuleType.DOMAIN_KEYWORD -> keywordMatcher.insert(rule.value, rule.exception)
            RuleType.URL_PATH -> urlMatcher.insert(rule.value, rule.exception)
            else -> {}
        }
    }

    /**
     * Completes Aho-Corasick failure mapping computations. Must be called after all inserts.
     */
    fun commit() {
        keywordMatcher.build()
    }

    fun matchDomain(domain: String): Pair<Boolean, Boolean>? {
        // 1. Exact map (O(1))
        val exact = exactMap.match(domain)
        if (exact != null) return exact

        // 2. Suffix Trie (O(Length))
        val suffix = suffixTrie.match(domain)
        if (suffix != null) return suffix

        // 3. Keyword Matcher (Aho-Corasick multi-pattern match)
        return keywordMatcher.match(domain)
    }

    fun matchUrl(path: String): Pair<Boolean, Boolean>? {
        return urlMatcher.match(path)
    }

    fun clear() {
        exactMap.clear()
        suffixTrie.clear()
        keywordMatcher.clear()
        urlMatcher.clear()
    }
}
