package com.aegisnet.firewall.engine

/**
 * Orchestrates the DomainTrie and AhoCorasickMatcher for a specific App.
 */
class DomainRuleMatcher {
    private val suffixTrie = DomainTrie()
    private val exactMatcher = AhoCorasickMatcher()
    
    // Store exact rules temporarily until build() is called
    private val pendingExactRules = mutableListOf<Pair<String, String>>()

    fun addRule(domain: String, action: String, matchType: String) {
        when (matchType) {
            "EXACT" -> pendingExactRules.add(Pair(domain, action))
            "SUFFIX", "WILDCARD" -> suffixTrie.insert(domain, action)
        }
    }

    fun build() {
        pendingExactRules.forEach { (domain, action) ->
            exactMatcher.insert(domain, action)
        }
        if (pendingExactRules.isNotEmpty()) {
            exactMatcher.build()
        }
        pendingExactRules.clear()
    }

    /**
     * Matches a domain against the rules.
     * Priorities: Exact Match > Suffix Match.
     * Returns "BLOCK", "ALLOW", or null.
     */
    fun match(domain: String): String? {
        // 1. Check exact match
        try {
            val exactResult = exactMatcher.findFirst(domain)
            if (exactResult != null && exactResult.first == domain) {
                 return exactResult.second
            }
        } catch (e: Exception) {
            // Ignored if not built
        }

        // 2. Check suffix / wildcard match
        return suffixTrie.searchSubdomain(domain)
    }
}
