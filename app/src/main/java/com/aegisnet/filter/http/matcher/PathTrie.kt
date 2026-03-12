package com.aegisnet.filter.http.matcher

import com.aegisnet.filter.http.HttpRule

class PathTrie {
    private val root = TrieNode()

    class TrieNode {
        val children = mutableMapOf<String, TrieNode>()
        var rule: HttpRule? = null
    }

    fun addRule(rule: HttpRule) {
        // e.g. /banners/ad.gif -> ["banners", "ad.gif"]
        val parts = rule.pattern.split("/").filter { it.isNotEmpty() }
        var current = root
        for (part in parts) {
            current = current.children.getOrPut(part) { TrieNode() }
        }
        current.rule = rule
    }

    fun match(path: String): HttpRule? {
        val parts = path.split("/").filter { it.isNotEmpty() }
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
