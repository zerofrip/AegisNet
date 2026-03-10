package com.aegisnet.ui.wireguard

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.entity.WgProfile
import com.aegisnet.wireguard.WireGuardImporter
import com.aegisnet.wireguard.WireGuardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WireGuardViewModel @Inject constructor(
    private val wireGuardManager: WireGuardManager,
    private val wireGuardImporter: WireGuardImporter
) : ViewModel() {

    private val _profiles = MutableStateFlow<List<WgProfile>>(emptyList())
    val profiles = _profiles.asStateFlow()

    private val _importResult = MutableStateFlow<Result<Int>?>(null)
    val importResult = _importResult.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            _profiles.value = wireGuardManager.getAllProfiles()
        }
    }

    fun importProfile(uri: Uri) {
        viewModelScope.launch {
            val result = wireGuardImporter.importFromUri(uri)
            _importResult.value = result
            if (result.isSuccess) {
                loadProfiles()
            }
        }
    }

    fun setActiveProfile(profile: WgProfile) {
        viewModelScope.launch {
            wireGuardManager.setActiveProfile(profile.id)
            loadProfiles()
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
