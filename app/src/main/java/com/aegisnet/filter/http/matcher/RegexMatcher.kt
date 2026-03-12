package com.aegisnet.filter.http.matcher

import com.aegisnet.filter.http.HttpRule

class RegexMatcher {
    // Group regex rules by a simple static substring they contain, to skip evaluation
    private val staticSignatureRules = mutableMapOf<String, MutableList<Pair<Regex, HttpRule>>>()
    private val generalRules = mutableListOf<Pair<Regex, HttpRule>>()

    fun addRule(rule: HttpRule) {
        try {
            val regex = Regex(rule.pattern)
            val pair = Pair(regex, rule)

            // Extremely simplified heuristic for this test: extract a 3+ letter word
            val signature = extractStaticSignature(rule.pattern)
            if (signature != null) {
                staticSignatureRules.getOrPut(signature) { mutableListOf() }.add(pair)
            } else {
                generalRules.add(pair)
            }
        } catch (e: Exception) {
            // Ignore invalid regex
        }
    }

    private fun extractStaticSignature(pattern: String): String? {
        // e.g., "^https?://.*/ad[0-9]+.js" -> look for contiguous letters
        val wordRegex = Regex("[a-zA-Z]{4,}")
        val match = wordRegex.find(pattern)
        return match?.value?.lowercase()
    }

    fun match(url: String): HttpRule? {
        val lowerUrl = url.lowercase()

        // 1. Evaluate only rules that have a static signature present in this url
        // Instead of looping all signatures, we just loop signatures that are mapped
        for ((sig, rules) in staticSignatureRules) {
            if (lowerUrl.contains(sig)) {
                for (i in 0 until rules.size) {
                    val pair = rules[i]
                    if (pair.first.containsMatchIn(url)) return pair.second
                }
            }
        }

        // 2. Evaluate general rules (ones without clear static signatures)
        for (i in 0 until generalRules.size) {
            val pair = generalRules[i]
            if (pair.first.containsMatchIn(url)) return pair.second
        }

        return null
    }
}
