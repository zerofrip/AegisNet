package com.aegisnet.ui.firewall

import androidx.lifecycle.ViewModel
import com.aegisnet.firewall.FirewallManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val firewallManager: FirewallManager
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val apps = combine(
        firewallManager.getInstalledAppsFlow(),
        searchQuery
    ) { appsList, query ->
        if (query.isBlank()) {
            appsList
        } else {
            appsList.filter { app ->
                app.appName.contains(query, ignoreCase = true) ||
                app.packageName.contains(query, ignoreCase = true)
            }
        }
    }
}
