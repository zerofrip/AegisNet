package com.aegisnet.firewall.engine

import java.util.LinkedList
import java.util.Queue

/**
 * Aho-Corasick implementation for rapid multi-pattern exact matching.
 */
class AhoCorasickMatcher {
    
    private class Node {
        val children = mutableMapOf<Char, Node>()
        var failLink: Node? = null
        val outputs = mutableListOf<String>()
        var ruleAction: String? = null // Action of the longest matched pattern
    }

    private var root = Node()
    private var isBuilt = false

    fun insert(keyword: String, action: String) {
        if (isBuilt) throw IllegalStateException("Cannot insert after building fail links")
        
        var current = root
        for (char in keyword) {
            current = current.children.getOrPut(char) { Node() }
        }
        current.outputs.add(keyword)
        current.ruleAction = action
    }

    fun build() {
        val queue: Queue<Node> = LinkedList()
        
        // Setup fail links for root children (depth 1)
        for (child in root.children.values) {
            child.failLink = root
            queue.add(child)
        }

        while (queue.isNotEmpty()) {
            val current = queue.poll()!!

            for ((char, child) in current.children) {
                queue.add(child)

                var fallback = current.failLink
                while (fallback != null && !fallback.children.containsKey(char)) {
                    fallback = fallback.failLink
                }
                
                child.failLink = fallback?.children?.get(char) ?: root
                
                // Inherit outputs from fail link
                child.failLink?.outputs?.let { child.outputs.addAll(it) }
                // Inherit action if not set locally (simplification for exact match scenarios)
                if (child.ruleAction == null) {
                    child.ruleAction = child.failLink?.ruleAction
                }
            }
        }
        isBuilt = true
    }

    /**
     * Finds the first matching pattern in the text.
     */
    fun findFirst(text: String): Pair<String, String>? {
        if (!isBuilt) throw IllegalStateException("Aho-Corasick not built")
        
        var current = root
        for (char in text) {
            while (current != root && !current.children.containsKey(char)) {
                current = current.failLink ?: root
            }
            current = current.children[char] ?: root

            if (current.outputs.isNotEmpty()) {
                // Determine action. In practice, returning the first output's action
                return Pair(current.outputs.first(), current.ruleAction ?: "BLOCK")
            }
        }
        return null
    }
    
    fun clear() {
        root = Node()
        isBuilt = false
    }
}
