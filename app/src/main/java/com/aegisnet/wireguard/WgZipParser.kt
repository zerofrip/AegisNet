package com.aegisnet.wireguard

import com.aegisnet.database.entity.WgProfile
import java.io.InputStream
import java.util.zip.ZipInputStream

object WgZipParser {

    /**
     * Parses a ZIP input stream containing multiple .conf files.
     * Returns a list of parsed WgProfiles.
     */
    fun parseZip(inputStream: InputStream): List<WgProfile> {
        val profiles = mutableListOf<WgProfile>()
        val zipInputStream = ZipInputStream(inputStream)

        zipInputStream.use { zis ->
            generateSequence { zis.nextEntry }.forEach { entry ->
                if (!entry.isDirectory && entry.name.endsWith(".conf", ignoreCase = true)) {
                    val content = zis.readBytes().toString(Charsets.UTF_8)
                    val name = entry.name.substringAfterLast("/").substringBeforeLast(".")
                    
                    WgConfigParser.parse(content, name)?.let { profile ->
                        profiles.add(profile)
                    }
                }
            }
        }

        return profiles
    }
}
