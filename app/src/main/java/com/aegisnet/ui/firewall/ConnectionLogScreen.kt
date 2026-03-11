package com.aegisnet.ui.firewall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionLogScreen(
    appUid: Int,
    onNavigateBack: () -> Unit,
    viewModel: ConnectionLogViewModel = hiltViewModel()
) {
    LaunchedEffect(appUid) {
        viewModel.loadLogs(appUid)
    }

    val logs by viewModel.logs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connection Logs") },
                navigationIcon = {
                    Button(onClick = onNavigateBack) { Text("Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(logs) { log ->
                val date = Date(log.timestamp)
                val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                
                ListItem(
                    headlineContent = { Text(log.domain) },
                    supportingContent = { Text(log.ip) },
                    trailingContent = { 
                        Text(
                            text = log.action,
                            color = if (log.action == "BLOCK") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    },
                    overlineContent = { Text(format.format(date)) }
                )
                Divider()
            }
        }
    }
}
