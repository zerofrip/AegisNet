package com.aegisnet.filter.parser

import com.aegisnet.filter.engine.RuleType
import com.aegisnet.filter.engine.UnifiedRule

/**
 * Handles comma-delimited Sing-Box/Clash specific blocks (e.g. DOMAIN-SUFFIX,example.com,DIRECT).
 */
object ClashParser {
    fun parse(line: String): UnifiedRule? {
        val trimmed = line.trim()
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return null
        }

        val parts = trimmed.split(",")
        if (parts.size >= 2) {
            val type = parts[0].trim().uppercase()
            val value = parts[1].trim()
            
            // Clash usually specifies action explicitely like REJECT, DIRECT
            var isException = false
            if (parts.size >= 3) {
                if (parts[2].trim().equals("DIRECT", ignoreCase = true)) {
                    isException = true
                }
            }

            return when (type) {
                "DOMAIN-SUFFIX" -> UnifiedRule(RuleType.DOMAIN_SUFFIX, value, isException)
                "DOMAIN" -> UnifiedRule(RuleType.EXACT_DOMAIN, value, isException)
                "DOMAIN-KEYWORD" -> UnifiedRule(RuleType.DOMAIN_KEYWORD, value, isException)
                else -> null
            }
        }
        return null
    }
}
