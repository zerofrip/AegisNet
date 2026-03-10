package com.aegisnet.dns

import com.aegisnet.database.dao.DnsProfileDao
import com.aegisnet.database.entity.DnsProfile
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DNSManager @Inject constructor(
    private val dnsProfileDao: DnsProfileDao
) {

    // Retrieves DNS profiles sorted by ID (simulating priority)
    suspend fun getActiveDnsProfiles(): List<DnsProfile> {
        return dnsProfileDao.getAll().first().filter { it.isActive }.sortedBy { it.id }
    }
}
