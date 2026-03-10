package com.aegisnet.ui.filters

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterListsScreen(onNavigateBack: () -> Unit) {
    // Stubbed data for UI mapping
    val items = remember { mutableStateOf(listOf("AdGuard Base", "OISD Full")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Lists") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { /* Add functionality */ }) {
                Text("Add List URL")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items.value) { item ->
                FilterListCard(
                    name = item,
                    url = "https://example.com/filter.txt",
                    lastUpdated = System.currentTimeMillis() - 86400000,
                    isEnabled = true,
                    onToggle = {},
                    onUpdate = {},
                    onDelete = {}
                )
            }
        }
    }
}

@Composable
fun FilterListCard(
    name: String,
    url: String,
    lastUpdated: Long,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onUpdate: () -> Unit,
    onDelete: () -> Unit
) {
    val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = if (lastUpdated > 0) df.format(Date(lastUpdated)) else "Never"

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Switch(checked = isEnabled, onCheckedChange = onToggle)
            }
            Text(url, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Last Updated: $dateString", style = MaterialTheme.typography.labelMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onUpdate) {
                    Icon(Icons.Default.Refresh, contentDescription = "Manual Update")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete List", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
