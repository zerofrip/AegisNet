package com.aegisnet.filter.http

import java.net.URI

data class ParsedUrl(
    val fullUrl: String,
    val domain: String,
    val path: String,
    val isHttps: Boolean
)

object URLParser {
    fun parse(url: String, defaultDomain: String? = null): ParsedUrl {
        // If it's just a domain (e.g. from SNI), handle it gracefully
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            val isHttps = true // Assume HTTPS if SNI-based
            val domain = defaultDomain ?: url.substringBefore("/")
            val path = if (url.contains("/")) "/" + url.substringAfter("/") else "/"
            return ParsedUrl(url, domain, path, isHttps)
        }

        return try {
            val uri = URI(url)
            val domain = uri.host ?: defaultDomain ?: ""
            val path = uri.path.takeIf { it.isNotEmpty() } ?: "/"
            val isHttps = uri.scheme == "https"
            ParsedUrl(url, domain, path, isHttps)
        } catch (e: Exception) {
            ParsedUrl(url, defaultDomain ?: "", "/", false)
        }
    }
}
