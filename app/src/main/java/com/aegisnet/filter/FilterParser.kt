package com.aegisnet.filter

import com.aegisnet.database.entity.UserRule

object FilterParser {

    /**
     * Parse AdGuard or uBlock syntax lines into Singbox-compatible domain rules.
     * This is a simplified demonstrative parser.
     */
    fun parseRuleLine(line: String): UserRule? {
        val trimmed = line.trim()
        
        // Ignore comments and empty lines
        if (trimmed.isEmpty() || trimmed.startsWith("!") || trimmed.startsWith("#")) {
            return null
        }

        // Example: ||ads.example.com^
        if (trimmed.startsWith("||") && trimmed.endsWith("^")) {
            val domain = trimmed.substring(2, trimmed.length - 1)
            return UserRule(rule = domain, isEnabled = true)
        }
        
        // Example: plain domain
        if (!trimmed.contains("/") && !trimmed.contains("*")) {
            return UserRule(rule = trimmed, isEnabled = true)
        }

        // Fallback: save as raw string
        return UserRule(rule = trimmed, isEnabled = true)
    }

    fun parseList(content: String): List<UserRule> {
        val rules = mutableListOf<UserRule>()
        content.lines().forEach { line ->
            parseRuleLine(line)?.let { rules.add(it) }
        }
        return rules
    }
}
