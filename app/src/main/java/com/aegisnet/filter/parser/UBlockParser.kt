package com.aegisnet.filter.parser

import com.aegisnet.filter.engine.RuleType
import com.aegisnet.filter.engine.UnifiedRule

/**
 * Translates uBlock Origin syntaxes into exact AegisNet UnifiedRules blocking network calls instantly.
 */
object UBlockParser {
    fun parse(line: String): UnifiedRule? {
        val trimmed = line.trim()
        
        // Ignore UBlock comments and frontend element hiders
        if (trimmed.isEmpty() || trimmed.startsWith("!") || 
            trimmed.contains("##") || trimmed.contains("#@#")) {
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
        }

        if (!content.contains("*") && !content.contains("/")) {
            return UnifiedRule(RuleType.EXACT_DOMAIN, content, exception)
        }
        return null
    }
}
