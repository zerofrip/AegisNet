package com.aegisnet.filter.store

/**
 * Optimized reverse Domain Trie for handling O(L) suffix matching.
 * e.g., ||ads.example.com^ matches test.ads.example.com instantly by reversing `com.example.ads.test`.
 */
class DomainSuffixTrie {
    class TrieNode {
        val children = HashMap<String, TrieNode>()
        var isEndOfRule = false
        var isException = false
    }

    private val root = TrieNode()

    fun insert(domain: String, exception: Boolean) {
        val parts = domain.split(".").reversed()
        var node = root
        for (part in parts) {
            if (!node.children.containsKey(part)) {
                node.children[part] = TrieNode()
            }
            node = node.children[part]!!
        }
        node.isEndOfRule = true
        node.isException = exception
    }

    /**
     * @return Pair<Boolean, Boolean> where first = Matched, second = IsException.
     * Returns null if no match found.
     */
    fun match(domain: String): Pair<Boolean, Boolean>? {
        val parts = domain.split(".").reversed()
        var node = root
        var hasMatch = false
        var isException = false

        for (part in parts) {
            node = node.children[part] ?: break
            if (node.isEndOfRule) {
                hasMatch = true
                if (node.isException) {
                    isException = true
                }
            }
        }
        
        return if (hasMatch) Pair(true, isException) else null
    }
    
    fun clear() {
        root.children.clear()
    }
}
