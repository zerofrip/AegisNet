package com.aegisnet.filter.http

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpRuleEngine @Inject constructor() {
    
    private val database = RuleDatabase()
    
    // Config flag from UI
    var isEnabled: Boolean = true

    fun ingestRules(rules: List<HttpRule>) {
        rules.forEach { database.addRule(it) }
    }

    /**
     * Match a given explicit URL or a domain name against all rules.
     * Returns the matching rule if blocked, or null if allowed.
     */
    fun match(url: String, domain: String? = null): HttpRule? {
        if (!isEnabled) return null
        
        val parsed = URLParser.parse(url, domain)

        // 1. Check Domain Rules (fastest)
        var match = database.domainTrie.match(parsed.domain)
        if (match != null) return checkException(match, parsed)

        // 2. Check Path Rules
        match = database.pathTrie.match(parsed.path)
        if (match != null) return checkException(match, parsed)

        // 3. Check Keyword Rules
        match = database.keywordMatcher.match(parsed.fullUrl)
        if (match != null) return checkException(match, parsed)

        // 4. Check Regex Rules (slowest)
        match = database.regexMatcher.match(parsed.fullUrl)
        if (match != null) return checkException(match, parsed)

        return null
    }

    private fun checkException(rule: HttpRule, parsed: ParsedUrl): HttpRule? {
        // In a full adblocker, we check if there's an exception rule (@@)
        // For simplicity, if the exact rule matched is an exception, allow it
        if (rule.isException) {
            return null
        }
        return rule
    }
}
