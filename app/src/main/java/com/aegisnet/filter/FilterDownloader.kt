package com.aegisnet.filter

import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class FilterDownloader @Inject constructor() {

    private val client = OkHttpClient()

    // Downloads list from URL. Validates HTTP response.
    suspend fun downloadList(url: String): String? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    return@withContext response.body?.string()
                } else {
                    return@withContext null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    // Parses string content into a list of rules
    fun parseRules(content: String): List<String> {
        return content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("!") && !it.startsWith("#") }
            .mapNotNull { line ->
                // Support basic hosts file domain extraction
                if (line.startsWith("127.0.0.1 ") || line.startsWith("0.0.0.0 ")) {
                    line.split(" ").lastOrNull()?.trim()
                } else {
                    line
                }
            }
    }
}
