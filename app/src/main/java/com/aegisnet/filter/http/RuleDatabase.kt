package com.aegisnet.filter.http

import com.aegisnet.filter.http.matcher.DomainTrie
import com.aegisnet.filter.http.matcher.KeywordMatcher
import com.aegisnet.filter.http.matcher.PathTrie
import com.aegisnet.filter.http.matcher.RegexMatcher

class RuleDatabase {
    val domainTrie = DomainTrie()
    val pathTrie = PathTrie()
    val keywordMatcher = KeywordMatcher()
    val regexMatcher = RegexMatcher()

    private var ruleCount = 0

    fun addRule(rule: HttpRule) {
        when (rule.type) {
            RuleType.DOMAIN -> domainTrie.addRule(rule)
            RuleType.PATH -> pathTrie.addRule(rule)
            RuleType.KEYWORD -> keywordMatcher.addRule(rule)
            RuleType.REGEX -> regexMatcher.addRule(rule)
        }
        ruleCount++
    }

    fun getRuleCount(): Int = ruleCount
    
    fun clear() {
        // In a real scenario, we might re-instantiate or explicitly clear maps
        ruleCount = 0
    }
}
