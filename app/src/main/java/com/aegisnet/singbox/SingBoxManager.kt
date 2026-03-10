package com.aegisnet.singbox

import com.aegisnet.dns.DNSManager
import com.aegisnet.filter.FilterManager
import com.aegisnet.routing.RoutingManager
import com.aegisnet.whitelist.WhitelistManager
import com.aegisnet.wireguard.WireGuardManager
import com.aegisnet.singbox.model.UserSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingBoxManager @Inject constructor(
    private val singBoxController: SingBoxController,
    private val configGenerator: ConfigGenerator,
    private val dnsManager: DNSManager,
    private val filterManager: FilterManager,
    private val whitelistManager: WhitelistManager,
    private val routingManager: RoutingManager,
    private val wireGuardManager: WireGuardManager
) {
    private var isRunning = false

    fun start(tunFd: Int) {
        if (isRunning) return
        
        // Assemble UserSettings snapshot from various managers
        val settings = runBlocking {
            val dnsProfiles = dnsManager.getActiveProfilesFlow().first()
            val filterLists = filterManager.getActiveFilterLists()
            val whitelistLists = whitelistManager.getActiveWhitelistLists()
            val userRules = filterManager.getUserRules()
            val routingRules = routingManager.getAllRules()
            val wgProfiles = wireGuardManager.getAllProfiles()
            val activeWg = wgProfiles.find { it.isActive }
            
            // Note: blockQuic and fakeDnsEnabled would ideally come from a SettingsManager/DataStore
            // For now, we assume defaults or fetch from a preference store.
            
            UserSettings(
                dnsServers = dnsProfiles,
                fakeDnsEnabled = true, // Defaulting to true for now
                filterLists = filterLists,
                whitelistLists = whitelistLists,
                userFilters = userRules.map { it.pattern },
                blockQuic = true, // Defaulting to true for now
                wireGuardProfiles = wgProfiles,
                activeWireGuardProfile = activeWg?.name,
                smartRoutingRules = routingRules
            )
        }

        // Generate JSON dynamically
        val configJson = configGenerator.build(settings)
        val errorMsg = singBoxController.startSingBox(configJson, tunFd)
        if (errorMsg.isEmpty()) {
            isRunning = true
        }
    }

    fun stop() {
        if (!isRunning) return
        singBoxController.stopSingBox()
        isRunning = false
    }

    fun isRunning(): Boolean {
        return isRunning
    }
}
