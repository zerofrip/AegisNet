package com.aegisnet.wireguard

import com.aegisnet.database.dao.WgProfileDao
import com.aegisnet.database.entity.WgProfile
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WireGuardManager @Inject constructor(
    private val wgProfileDao: WgProfileDao
) {

    suspend fun getActiveProfile(): WgProfile? {
        // Enforces only one active profile
        return wgProfileDao.getAll().first().firstOrNull { it.isActive }
    }

    suspend fun setActiveProfile(profileId: Long) {
        val profiles = wgProfileDao.getAll().first()
        profiles.forEach { profile ->
            if (profile.id == profileId) {
                if (!profile.isActive) {
                    wgProfileDao.insert(profile.copy(isActive = true))
                }
            } else if (profile.isActive) {
                wgProfileDao.insert(profile.copy(isActive = false))
            }
        }
    }
}
