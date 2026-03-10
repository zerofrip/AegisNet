package com.aegisnet.filter.store

import java.util.concurrent.ConcurrentHashMap

/**
 * Rapid 1:1 Domain hashing for strict blocks or whitelist mappings.
 */
class ExactDomainMap {
    private val map = ConcurrentHashMap<String, Boolean>() // K: Domain, V: isException

    fun insert(domain: String, exception: Boolean) {
        map[domain] = exception
    }

    fun match(domain: String): Pair<Boolean, Boolean>? {
        val isException = map[domain]
        if (isException != null) {
            return Pair(true, isException)
        }
        return null
    }

    fun clear() {
        map.clear()
    }
}
