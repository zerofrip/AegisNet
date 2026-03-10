package com.aegisnet.wireguard

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WireGuardImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wireGuardManager: WireGuardManager
) {

    suspend fun importFromUri(uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(uri) ?: "ImportedProfile"
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                if (fileName.endsWith(".conf", ignoreCase = true)) {
                    val content = inputStream.bufferedReader().readText()
                    val profile = WgConfigParser.parse(content, fileName.substringBeforeLast("."))
                    if (profile != null) {
                        wireGuardManager.addProfile(profile)
                        return@withContext Result.success(1)
                    } else {
                        return@withContext Result.failure(Exception("Invalid WireGuard configuration"))
                    }
                } else if (fileName.endsWith(".zip", ignoreCase = true)) {
                    val profiles = WgZipParser.parseZip(inputStream)
                    if (profiles.isNotEmpty()) {
                        wireGuardManager.addProfiles(profiles)
                        return@withContext Result.success(profiles.size)
                    } else {
                        return@withContext Result.failure(Exception("No valid configuration files found in ZIP"))
                    }
                } else {
                    return@withContext Result.failure(Exception("Unsupported file type"))
                }
            } ?: Result.failure(Exception("Could not open input stream"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFileName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        } ?: uri.path?.substringAfterLast("/")
    }
}
