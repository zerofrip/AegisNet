package com.aegisnet.filter.http.matcher

import com.aegisnet.filter.http.HttpRule

class KeywordMatcher {
    private val ruleMap = mutableMapOf<String, HttpRule>()

    fun addRule(rule: HttpRule) {
        ruleMap[rule.pattern] = rule
    }

    fun match(url: String): HttpRule? {
        // Simple iteration - in production with 10k keywords, consider Aho-Corasick automaton
        for ((keyword, rule) in ruleMap) {
            if (url.contains(keyword)) {
                return rule
            }
        }
        return null
    }

    // Fast-path: check if a specific keyword matches
    fun matchKeyword(keyword: String): HttpRule? {
        return ruleMap[keyword]
    }
}
