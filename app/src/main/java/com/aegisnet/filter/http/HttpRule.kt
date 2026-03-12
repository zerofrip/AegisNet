package com.aegisnet.filter.http

enum class RuleType {
    DOMAIN,
    PATH,
    KEYWORD,
    REGEX
}

data class HttpRule(
    val rawRule: String,
    val type: RuleType,
    val pattern: String,
    val isException: Boolean = false // e.g. @@||example.com^
)
