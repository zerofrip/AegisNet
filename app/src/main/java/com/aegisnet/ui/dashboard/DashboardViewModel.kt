package com.aegisnet.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.dns.DNSManager
import com.aegisnet.singbox.SingBoxController
import com.aegisnet.singbox.SingBoxManager
import com.aegisnet.wireguard.WireGuardManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val isVpnActive: Boolean = false,
    val isConnecting: Boolean = false,
    val txBytes: Long = 0,
    val rxBytes: Long = 0,
    val blockedCount: Long = 0,
    val activeDns: String = "Default",
    val activeWireGuard: String = "None"
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val singBoxManager: SingBoxManager,
    private val singBoxController: SingBoxController,
    private val dnsManager: DNSManager,
    private val wireGuardManager: WireGuardManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state = _state.asStateFlow()

    init {
        observeVpnState()
        pollTrafficStats()
        observeActiveSettings()
    }

    fun onConnectPressed() {
        _state.update { it.copy(isConnecting = true) }
    }

    fun onDisconnectPressed() {
        _state.update { it.copy(isConnecting = true) }
    }

    fun onConnectCancelled() {
        _state.update { it.copy(isConnecting = false) }
    }

    private fun observeVpnState() {
        viewModelScope.launch {
            singBoxManager.isRunningState.collect { isRunning ->
                _state.update { it.copy(isVpnActive = isRunning, isConnecting = false) }
            }
        }
    }

    private fun pollTrafficStats() {
        viewModelScope.launch {
            while (true) {
                if (singBoxManager.isRunning()) {
                    try {
                        val stats = singBoxController.getTrafficStats()
                        val blocked = singBoxController.getBlockedCount()
                        _state.update {
                            it.copy(
                                txBytes = stats.getOrElse(0) { 0L },
                                rxBytes = stats.getOrElse(1) { 0L },
                                blockedCount = blocked
                            )
                        }
                    } catch (_: Exception) { }
                }
                delay(1000)
            }
        }
    }

    private fun observeActiveSettings() {
        viewModelScope.launch {
            combine(
                flow { emit(dnsManager.getActiveDnsProfiles()) },
                flow { emit(wireGuardManager.getActiveProfile()) } // Simplification, ideally active profile should be a Flow
            ) { dns, wg ->
                _state.update { it.copy(
                    activeDns = dns.firstOrNull()?.name ?: "Default",
                    activeWireGuard = wg?.name ?: "None"
                ) }
            }.collect()
        }
    }
}
