package com.aegisnet.filter.engine

enum class RuleType {
    EXACT_DOMAIN,
    DOMAIN_SUFFIX,
    DOMAIN_KEYWORD,
    URL_PATH,
    URL_REGEX,
    UNKNOWN
}

data class UnifiedRule(
    val type: RuleType,
    val value: String,
    val exception: Boolean,
    val options: Map<String, String>? = null
)
