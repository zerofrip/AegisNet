package com.aegisnet.filter.http.parser

import com.aegisnet.filter.http.HttpRule

class UBlockHttpParser : HttpRuleParser {
    // For this scope, uBlock basic network rules map very similarly to AdGuard basic filters
    private val adGuardParser = AdGuardHttpParser()
    
    override fun parse(line: String): HttpRule? {
        val trimmed = line.trim()
        // Skip cosmetics
        if (trimmed.contains("##") || trimmed.contains("#?#")) {
            return null
        }
        return adGuardParser.parse(line)
    }
}
