package com.aegisnet.singbox

import android.util.Log
import com.aegisnet.dns.DNSManager
import com.aegisnet.filter.FilterManager
import com.aegisnet.routing.SmartRoutingEngine
import com.aegisnet.whitelist.WhitelistManager
import com.aegisnet.wireguard.WireGuardManager
import com.aegisnet.singbox.model.UserSettings
import com.aegisnet.database.dao.FilterListDao
import com.aegisnet.database.dao.WhitelistListDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private val wireGuardManager: WireGuardManager,
    private val smartRoutingEngine: SmartRoutingEngine,
    private val filterListDao: FilterListDao,
    private val whitelistListDao: WhitelistListDao
) {
    private val _isRunning = MutableStateFlow(false)
    val isRunningState: StateFlow<Boolean> = _isRunning.asStateFlow()

    fun isRunning(): Boolean = _isRunning.value

    fun start(tunFd: Int) {
        if (isRunning()) {
            Log.i("SingBoxManager", "Stopping existing engine before restart")
            stop()
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i("SingBoxManager", "Assembling UserSettings snapshot...")
                val settings = runBlocking {
                    val dnsProfiles = dnsManager.getActiveDnsProfiles()
                    val filterLists = filterListDao.getAll().first()
                    val whitelistLists = whitelistListDao.getAll().first()
                    val userRules = filterManager.getActiveBlockDomains()
                    val whitelistDomains = whitelistManager.getActiveWhitelistDomains()
                    val wgProfiles = wireGuardManager.getAllProfiles()
                    val activeWg = wgProfiles.find { it.isActive }
                    val routingRules = smartRoutingEngine.getActiveRules()
                    
                    UserSettings(
                        dnsServers = dnsProfiles,
                        fakeDnsEnabled = true,
                        filterLists = filterLists,
                        whitelistLists = whitelistLists,
                        userFilters = userRules,
                        whitelistDomains = whitelistDomains,
                        blockQuic = true,
                        wireGuardProfiles = wgProfiles,
                        activeWireGuardProfile = activeWg,
                        smartRoutingRules = routingRules
                    )
                }

                Log.i("SingBoxManager", "Generating config...")
                val configJson = configGenerator.build(settings)
                
                Log.i("SingBoxManager", "Calling JNI startSingBox with FD: $tunFd")
                val errorMsg = singBoxController.startSingBox(configJson, tunFd)
                if (errorMsg.isNotEmpty()) {
                    Log.e("SingBoxManager", "JNI Start Error: $errorMsg")
                    _isRunning.value = false
                } else {
                    Log.i("SingBoxManager", "JNI Start Success")
                    _isRunning.value = true
                }
            } catch (e: Exception) {
                Log.e("SingBoxManager", "Error in start flow: ${e.message}", e)
                _isRunning.value = false
            }
        }
    }

    fun stop() {
        Log.i("SingBoxManager", "Stopping sing-box engine")
        singBoxController.stopSingBox()
        _isRunning.value = false
    }
}
