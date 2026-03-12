package com.aegisnet.filter.http.parser

import com.aegisnet.filter.http.HttpRule
import com.aegisnet.filter.http.RuleType

class AdGuardHttpParser : HttpRuleParser {
    override fun parse(line: String): HttpRule? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("!") || trimmed.startsWith("#")) {
            return null
        }

        var isException = false
        var ruleStr = trimmed

        // Handle exceptions
        if (ruleStr.startsWith("@@")) {
            isException = true
            ruleStr = ruleStr.substring(2)
        }

        return when {
            // Regex rule
            ruleStr.startsWith("/") && ruleStr.endsWith("/") -> {
                HttpRule(trimmed, RuleType.REGEX, ruleStr.substring(1, ruleStr.length - 1), isException)
            }
            // Strict domain match
            ruleStr.startsWith("||") && ruleStr.endsWith("^") -> {
                HttpRule(trimmed, RuleType.DOMAIN, ruleStr.substring(2, ruleStr.length - 1), isException)
            }
            // Domain rule (AdGuard style with modifier)
            ruleStr.startsWith("||") -> {
                val domainPart = ruleStr.substring(2).substringBefore("^")
                HttpRule(trimmed, RuleType.DOMAIN, domainPart, isException)
            }
            // Keyword match
            ruleStr.startsWith("*") && ruleStr.endsWith("*") -> {
                val keyword = ruleStr.substring(1, ruleStr.length - 1)
                HttpRule(trimmed, RuleType.KEYWORD, keyword, isException)
            }
            // Default to Path rule if it contains a slash, else Domain
            ruleStr.contains("/") -> {
                HttpRule(trimmed, RuleType.PATH, ruleStr, isException)
            }
            else -> {
                // simple fallback
                HttpRule(trimmed, RuleType.KEYWORD, ruleStr, isException)
            }
        }
    }
}
