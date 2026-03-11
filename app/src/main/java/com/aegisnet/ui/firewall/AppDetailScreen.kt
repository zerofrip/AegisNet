package com.aegisnet.ui.firewall

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    appUid: Int,
    onNavigateBack: () -> Unit,
    onNavigateToDomainRules: (Int) -> Unit,
    onNavigateToConnectionLogs: (Int) -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(appUid) {
        viewModel.loadAppDetails(appUid)
    }

    val appInfo by viewModel.appInfo.collectAsState()
    val routingRule by viewModel.routingRule.collectAsState()
    val dnsRule by viewModel.dnsRule.collectAsState()
    val trafficStats by viewModel.trafficStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appInfo?.appName ?: "App Details") },
                navigationIcon = {
                    Button(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Traffic Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Upload: ${trafficStats?.uploadBytes ?: 0} B")
                    Text("Download: ${trafficStats?.downloadBytes ?: 0} B")
                    Text("Connections: ${trafficStats?.connectionCount ?: 0}")
                }
            }

            // Routing Mode
            var selectedMode by remember { mutableStateOf(routingRule?.routeMode ?: "WIREGUARD") }
            Text("Routing Mode", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DIRECT", "WIREGUARD", "BLOCK", "BYPASS").forEach { mode ->
                    FilterChip(
                        selected = selectedMode == mode,
                        onClick = { 
                            selectedMode = mode
                            viewModel.updateRoutingMode(mode) 
                        },
                        label = { Text(mode) }
                    )
                }
            }

            // Toggles
            var blockQuic by remember { mutableStateOf(routingRule?.blockQuic ?: false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Block QUIC (UDP 443)")
                Switch(
                    checked = blockQuic,
                    onCheckedChange = { 
                        blockQuic = it
                        viewModel.updateQuicBlocking(it) 
                    }
                )
            }

            Divider()

            // Navigation
            Button(
                onClick = { onNavigateToDomainRules(appUid) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Domain Rules")
            }

            Button(
                onClick = { onNavigateToConnectionLogs(appUid) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connection Logs")
            }
        }
    }
}
