package com.aegisnet.filter.cache

import java.util.LinkedHashMap

/**
 * A simple thread-safe LRU Cache to persist domain resolutions matching Sub-1ms criteria.
 */
class LRUCache<K, V>(private val maxEntries: Int) {
    
    private val cache = object : LinkedHashMap<K, V>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
            return size > maxEntries
        }
    }

    @Synchronized
    fun get(key: K): V? {
        return cache[key]
    }

    @Synchronized
    fun put(key: K, value: V) {
        cache[key] = value
    }

    @Synchronized
    fun clear() {
        cache.clear()
    }
    
    @Synchronized
    fun size(): Int {
        return cache.size
    }
}
