package com.aegisnet.ui.filter.http

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HTTPFilterSettingsScreen(
    isEngineEnabled: Boolean,
    onEnableChanged: (Boolean) -> Unit,
    onNavigateToRules: () -> Unit,
    onNavigateToCustomRules: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("HTTP Filtering") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Toggle Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Enable HTTP Filtering", style = MaterialTheme.typography.titleMedium)
                        Text("Deep packet inspection for ad & tracker blocking", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = isEngineEnabled,
                        onCheckedChange = onEnableChanged
                    )
                }
            }

            Text("Configuration", style = MaterialTheme.typography.titleMedium)
            
            ListItem(
                headlineContent = { Text("Active Rules") },
                supportingContent = { Text("View loaded AdGuard, uBlock, and Clash rules") },
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToRules() },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )

            ListItem(
                headlineContent = { Text("Custom Rules") },
                supportingContent = { Text("Add your own domain, keyword, or regex rules") },
                modifier = Modifier.fillMaxWidth().clickable { onNavigateToCustomRules() },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
