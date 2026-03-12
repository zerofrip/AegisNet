package com.aegisnet.filter.http.matcher

import com.aegisnet.filter.http.HttpRule

class DomainTrie {
    private val root = TrieNode()

    class TrieNode {
        val children = mutableMapOf<String, TrieNode>()
        var rule: HttpRule? = null
    }

    fun addRule(rule: HttpRule) {
        // e.g. ads.example.com -> process backwards -> "com", "example", "ads"
        val parts = rule.pattern.split(".").reversed()
        var current = root

        for (part in parts) {
            current = current.children.getOrPut(part) { TrieNode() }
        }
        current.rule = rule
    }

    fun match(domain: String): HttpRule? {
        val parts = domain.split(".").reversed()
        var current = root

        for (part in parts) {
            current = current.children[part] ?: return null
            if (current.rule != null) {
                return current.rule
            }
        }
        return null
    }
}
