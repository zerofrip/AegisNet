package com.aegisnet.filter.store

import java.util.LinkedList
import java.util.Queue

/**
 * Aho-Corasick implementation for parsing multi-pattern Wildcard / Keyword rules globally.
 * Matches keywords like `*tracker*` across the entire URI space simultaneously.
 */
class KeywordMatcher {
    class OutputMatch(val keyword: String, val isException: Boolean)

    class ACNode {
        val children = HashMap<Char, ACNode>()
        var fail: ACNode? = null
        val outputs = mutableListOf<OutputMatch>()
    }

    private val root = ACNode()
    private var isBuilt = false

    fun insert(keyword: String, exception: Boolean) {
        var node = root
        for (char in keyword) {
            if (!node.children.containsKey(char)) {
                node.children[char] = ACNode()
            }
            node = node.children[char]!!
        }
        node.outputs.add(OutputMatch(keyword, exception))
        isBuilt = false
    }

    fun build() {
        val queue: Queue<ACNode> = LinkedList()
        for (child in root.children.values) {
            child.fail = root
            queue.offer(child)
        }

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            for ((char, child) in current.children) {
                var failNode = current.fail
                while (failNode != null && !failNode.children.containsKey(char)) {
                    failNode = failNode.fail
                }
                child.fail = failNode?.children?.get(char) ?: root
                child.outputs.addAll(child.fail!!.outputs)
                queue.offer(child)
            }
        }
        isBuilt = true
    }

    fun match(text: String): Pair<Boolean, Boolean>? {
        if (!isBuilt) build()
        var node = root
        var hasMatch = false
        var isException = false

        for (char in text) {
            while (node != root && !node.children.containsKey(char)) {
                node = node.fail ?: root
            }
            node = node.children[char] ?: root
            
            if (node.outputs.isNotEmpty()) {
                hasMatch = true
                if (node.outputs.any { it.isException }) {
                    isException = true
                }
            }
        }
        return if (hasMatch) Pair(true, isException) else null
    }

    fun clear() {
        root.children.clear()
        isBuilt = false
    }
}
