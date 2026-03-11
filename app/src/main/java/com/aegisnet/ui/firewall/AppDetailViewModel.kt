package com.aegisnet.ui.firewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.AppDNSRuleDao
import com.aegisnet.database.dao.AppInfoDao
import com.aegisnet.database.dao.AppRoutingRuleDao
import com.aegisnet.database.dao.TrafficStatsDao
import com.aegisnet.database.entity.AppDNSRule
import com.aegisnet.database.entity.AppInfo
import com.aegisnet.database.entity.AppRoutingRule
import com.aegisnet.database.entity.TrafficStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    private val appInfoDao: AppInfoDao,
    private val routingRuleDao: AppRoutingRuleDao,
    private val dnsRuleDao: AppDNSRuleDao,
    private val trafficStatsDao: TrafficStatsDao
) : ViewModel() {

    private var currentAppUid: Int = -1

    private val _appInfo = MutableStateFlow<AppInfo?>(null)
    val appInfo: StateFlow<AppInfo?> = _appInfo

    private val _routingRule = MutableStateFlow<AppRoutingRule?>(null)
    val routingRule: StateFlow<AppRoutingRule?> = _routingRule

    private val _dnsRule = MutableStateFlow<AppDNSRule?>(null)
    val dnsRule: StateFlow<AppDNSRule?> = _dnsRule

    private val _trafficStats = MutableStateFlow<TrafficStats?>(null)
    val trafficStats: StateFlow<TrafficStats?> = _trafficStats

    fun loadAppDetails(uid: Int) {
        currentAppUid = uid
        viewModelScope.launch {
            _appInfo.value = appInfoDao.getAll().firstOrNull()?.find { it.uid == uid }
            
            routingRuleDao.getRuleForApp(uid).collect { rule ->
                _routingRule.value = rule ?: AppRoutingRule(uid, "WIREGUARD", false, false)
            }
        }
        viewModelScope.launch {
            dnsRuleDao.getRuleForApp(uid).collect { rule ->
                _dnsRule.value = rule
            }
        }
        viewModelScope.launch {
            trafficStatsDao.getStatsForApp(uid).collect { stats ->
                _trafficStats.value = stats
            }
        }
    }

    fun updateRoutingMode(mode: String) {
        viewModelScope.launch {
            val current = _routingRule.value ?: AppRoutingRule(currentAppUid, "WIREGUARD", false, false)
            routingRuleDao.insert(current.copy(routeMode = mode, bypassVpn = mode == "BYPASS"))
        }
    }

    fun updateQuicBlocking(block: Boolean) {
        viewModelScope.launch {
             val current = _routingRule.value ?: AppRoutingRule(currentAppUid, "WIREGUARD", false, false)
             routingRuleDao.insert(current.copy(blockQuic = block))
        }
    }
}
