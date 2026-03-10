package com.aegisnet.filter.parser

import com.aegisnet.filter.engine.RuleType
import com.aegisnet.filter.engine.UnifiedRule

/**
 * Normalizes AdGuard rule strings into AegisNet UnifiedRule datasets, safely scrubbing cosmetic/CSS payloads.
 */
object AdGuardParser {
    fun parse(line: String): UnifiedRule? {
        val trimmed = line.trim()
        
        // Ignore empty lines, comments, and pure cosmetic modifiers (##, #?#, #@#)
        if (trimmed.isEmpty() || trimmed.startsWith("!") || 
            trimmed.contains("##") || trimmed.contains("#?#") || trimmed.contains("#@#")) {
            return null
        }

        var exception = false
        var content = trimmed

        if (content.startsWith("@@")) {
            exception = true
            content = content.substring(2)
        }

        if (content.startsWith("||") && content.endsWith("^")) {
            return UnifiedRule(RuleType.DOMAIN_SUFFIX, content.substring(2, content.length - 1), exception)
        } else if (content.startsWith("||")) {
            return UnifiedRule(RuleType.DOMAIN_SUFFIX, content.substring(2), exception)
        } else if (content.startsWith("*") && content.endsWith("*")) {
            return UnifiedRule(RuleType.DOMAIN_KEYWORD, content.substring(1, content.length - 1), exception)
        } else if (content.startsWith("/")) {
            return UnifiedRule(RuleType.URL_PATH, content.substringBeforeLast("^").trimEnd('/'), exception)
        }

        if (!content.contains("*")) {
            return UnifiedRule(RuleType.EXACT_DOMAIN, content, exception)
        }
        return null
    }
}
