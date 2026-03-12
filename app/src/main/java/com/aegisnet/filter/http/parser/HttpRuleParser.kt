package com.aegisnet.filter.http.parser

import com.aegisnet.filter.http.HttpRule
import com.aegisnet.filter.http.RuleType

interface HttpRuleParser {
    /**
     * Parse a single rule line. 
     * Returns null if the line is a comment or unsupported format.
     */
    fun parse(line: String): HttpRule?
}
