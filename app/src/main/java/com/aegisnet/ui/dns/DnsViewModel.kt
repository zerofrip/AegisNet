package com.aegisnet.ui.dns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.DnsProfileDao
import com.aegisnet.database.entity.DnsProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DnsViewModel @Inject constructor(
    private val dnsProfileDao: DnsProfileDao
) : ViewModel() {

    val profiles = dnsProfileDao.getAll()

    fun addProfile(name: String, type: String, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newProfile = DnsProfile(
                name = name,
                type = type,
                serverUrl = url,
                isActive = false
            )
            dnsProfileDao.insert(newProfile)
        }
    }

    fun activateProfile(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dnsProfileDao.deactivateAll()
            dnsProfileDao.activateProfile(profileId)
        }
    }

    fun deleteProfile(profile: DnsProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            dnsProfileDao.delete(profile)
        }
    }
}
