package com.aegisnet.ui.wireguard

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisnet.database.entity.WgProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WireGuardSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: WireGuardViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val importResult by viewModel.importResult.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importProfile(it) }
    }

    importResult?.let { result ->
        AlertDialog(
            onDismissRequest = { viewModel.clearImportResult() },
            title = { Text(if (result.isSuccess) "Import Successful" else "Import Failed") },
            text = { Text(if (result.isSuccess) "Imported ${result.getOrNull()} profiles." else result.exceptionOrNull()?.message ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearImportResult() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WireGuard Profiles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                // Supports both .conf and .zip
                filePicker.launch("*/*") 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Import Profile")
            }
        }
    ) { padding ->
        if (profiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No profiles imported. Tap + to add.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(profiles) { profile ->
                    ProfileItem(
                        profile = profile,
                        onClick = { viewModel.setActiveProfile(profile) }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileItem(profile: WgProfile, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(profile.name) },
        supportingContent = { Text(profile.endpoint) },
        trailingContent = {
            if (profile.isActive) {
                Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
            }
        }
    )
}
