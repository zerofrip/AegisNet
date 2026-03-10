package com.aegisnet.whitelist

import com.aegisnet.database.dao.WhitelistRuleDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhitelistManager @Inject constructor(
    private val whitelistRuleDao: WhitelistRuleDao
) {
    suspend fun getActiveWhitelistDomains(): List<String> {
        // Simple string extraction of active domains.
        return whitelistRuleDao.getAll().first().filter { it.isEnabled }.map { it.domain }
    }
}
