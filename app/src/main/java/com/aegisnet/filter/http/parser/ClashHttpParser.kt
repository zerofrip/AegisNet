package com.aegisnet.filter.http.parser

import com.aegisnet.filter.http.HttpRule
import com.aegisnet.filter.http.RuleType

class ClashHttpParser : HttpRuleParser {
    override fun parse(line: String): HttpRule? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) return null

        // e.g., DOMAIN-SUFFIX,ads.example.com,REJECT
        val parts = trimmed.split(",")
        if (parts.size < 2) return null

        val typeStr = parts[0]
        val pattern = parts[1]
        // val action = parts.getOrNull(2) // Assumed REJECT/DROP for rules loaded here

        return when {
            typeStr.startsWith("DOMAIN-SUFFIX") || typeStr.startsWith("DOMAIN") -> {
                HttpRule(trimmed, RuleType.DOMAIN, pattern, false)
            }
            typeStr.startsWith("DOMAIN-KEYWORD") -> {
                HttpRule(trimmed, RuleType.KEYWORD, pattern, false)
            }
            else -> null
        }
    }
}
