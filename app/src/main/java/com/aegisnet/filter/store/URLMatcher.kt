package com.aegisnet.filter.store

/**
 * Tree structure representing HTTP URL Path sequences (e.g. /ads/banner/js) for fast sub-path block logic.
 */
class URLMatcher {
    class PathNode {
        val children = HashMap<String, PathNode>()
        var isEndOfRule = false
        var isException = false
    }

    private val root = PathNode()

    fun insert(path: String, exception: Boolean) {
        val parts = path.split("/").filter { it.isNotEmpty() }
        var node = root
        for (part in parts) {
            if (!node.children.containsKey(part)) {
                node.children[part] = PathNode()
            }
            node = node.children[part]!!
        }
        node.isEndOfRule = true
        node.isException = exception
    }

    fun match(path: String): Pair<Boolean, Boolean>? {
        val parts = path.split("/").filter { it.isNotEmpty() }
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
