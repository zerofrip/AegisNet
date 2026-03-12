package com.aegisnet.ui.firewall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisnet.database.entity.AppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(
    onNavigateToAppDetail: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AppsViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsState(initial = emptyList())
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Firewall") },
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search Apps...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(apps) { app ->
                    AppListItem(app = app, onClick = { onNavigateToAppDetail(app.uid) })
                }
            }
        }
    }
}

@Composable
fun AppListItem(app: AppInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = app.appName, style = MaterialTheme.typography.titleMedium)
                Text(text = app.packageName, style = MaterialTheme.typography.bodySmall)
            }
            if (app.isSystemApp) {
                Badge { Text("System") }
            }
        }
    }
}
