package com.aegisnet.ui.dashboard

import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisnet.vpn.AegisVpnService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToApps: () -> Unit,
    onNavigateToDns: () -> Unit,
    onNavigateToFilters: () -> Unit,

    onNavigateToWhitelist: () -> Unit,
    onNavigateToRouting: () -> Unit,
    onNavigateToWireGuard: () -> Unit,
    onNavigateToLicenses: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogs: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val vpnServiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            startVpnService(context)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AegisNet Dashboard") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (state.isVpnActive) "VPN Connected" else "VPN Disconnected",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (state.isVpnActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        if (state.isVpnActive) {
                            stopVpnService(context)
                        } else {
                            val intent = VpnService.prepare(context)
                            if (intent != null) {
                                vpnServiceLauncher.launch(intent)
                            } else {
                                startVpnService(context)
                            }
                        }
                    }) {
                        Text(if (state.isVpnActive) "Disconnect" else "Connect")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Current DNS:", style = MaterialTheme.typography.bodyMedium)
                        Text(state.activeDns, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("WireGuard Profile:", style = MaterialTheme.typography.bodyMedium)
                        Text(state.activeWireGuard, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Blocked Requests:", style = MaterialTheme.typography.bodyMedium)
                        Text(state.blockedCount.toString(), style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Traffic Tx / Rx:", style = MaterialTheme.typography.bodyMedium)
                        Text("${formatBytes(state.txBytes)} / ${formatBytes(state.rxBytes)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Button(onClick = onNavigateToApps, modifier = Modifier.fillMaxWidth()) {
                Text("App Firewall")
            }
            Button(onClick = onNavigateToDns, modifier = Modifier.fillMaxWidth()) {
                Text("DNS Settings")
            }
            Button(onClick = onNavigateToFilters, modifier = Modifier.fillMaxWidth()) {
                Text("AdBlock / Filter Lists")
            }
            Button(onClick = onNavigateToWhitelist, modifier = Modifier.fillMaxWidth()) {
                Text("Whitelist Lists")
            }
            Button(onClick = onNavigateToRouting, modifier = Modifier.fillMaxWidth()) {
                Text("Smart Routing Rules")
            }
            Button(onClick = onNavigateToWireGuard, modifier = Modifier.fillMaxWidth()) {
                Text("WireGuard Profiles")
            }
            Button(onClick = onNavigateToSettings, modifier = Modifier.fillMaxWidth()) {
                Text("Settings & Update Intervals")
            }
            Button(onClick = onNavigateToLogs, modifier = Modifier.fillMaxWidth()) {
                Text("System Logs")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onNavigateToLicenses) {
                Text("Open Source Licenses")
            }
        }
    }
}

private fun startVpnService(context: Context) {
    val intent = Intent(context, AegisVpnService::class.java).apply {
        action = AegisVpnService.ACTION_START
    }
    context.startForegroundService(intent)
}

private fun stopVpnService(context: Context) {
    val intent = Intent(context, AegisVpnService::class.java).apply {
        action = AegisVpnService.ACTION_STOP
    }
    context.startForegroundService(intent)
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "%.2f KB".format(bytes / 1024.0)
    if (bytes < 1024 * 1024 * 1024) return "%.2f MB".format(bytes / (1024.0 * 1024.0))
    return "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
}
