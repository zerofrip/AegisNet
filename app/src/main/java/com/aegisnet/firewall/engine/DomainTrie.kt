package com.aegisnet.firewall.engine

/**
 * Highly optimized Trie for matching domain suffixes and wildcards.
 */
class DomainTrie {
    
    private class Node {
        val children = mutableMapOf<Char, Node>()
        var isEndOfDomain = false
        var ruleAction: String? = null // "BLOCK" or "ALLOW"
    }

    private val root = Node()

    /**
     * Inserts a domain into the Trie.
     * To match exact or suffix, we insert the domain in reverse.
     * So "example.com" is inserted as "m.o.c.e.l.p.m.a.x.e"
     */
    fun insert(domain: String, action: String) {
        var current = root
        for (i in domain.length - 1 downTo 0) {
            val char = domain[i]
            current = current.children.getOrPut(char) { Node() }
        }
        current.isEndOfDomain = true
        current.ruleAction = action
    }

    /**
     * Searches for a domain or its suffixes in the Trie.
     * Returns the action associated with the longest matching suffix, or null.
     */
    fun searchSubdomain(domain: String): String? {
        var current = root
        var lastMatchedAction: String? = null
        
        for (i in domain.length - 1 downTo 0) {
            val char = domain[i]
            current = current.children[char] ?: break
            
            // If the current node is an endpoint, check if it's a valid suffix boundary
            // e.g. "com.example" matches "com.example.sub", but "m" should not match "com"
            if (current.isEndOfDomain) {
                // Must either completely match the string, or the next char in the original string must be a dot
                if (i == 0 || domain[i - 1] == '.') {
                    lastMatchedAction = current.ruleAction
                }
            }
        }
        
        return lastMatchedAction
    }
    
    fun clear() {
        root.children.clear()
        root.isEndOfDomain = false
        root.ruleAction = null
    }
}
