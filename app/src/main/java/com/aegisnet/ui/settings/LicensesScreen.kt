package com.aegisnet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Open Source Licenses") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AegisNet is licensed under the MIT License.\n" +
                       "This application contains open source software.",
                style = MaterialTheme.typography.bodyLarge
            )
            
            Divider()

            LicenseItem(
                name = "sing-box",
                license = "BSD-3-Clause",
                description = "Core networking engine, providing routing, filtering, " +
                              "and WireGuard outbound support."
            )
            
            LicenseItem(
                name = "Kotlin",
                license = "Apache License 2.0",
                description = "Programming language used for Android development."
            )

            LicenseItem(
                name = "AndroidX & Jetpack Compose",
                license = "Apache License 2.0",
                description = "UI toolkit and core Android libraries."
            )

            LicenseItem(
                name = "Retrofit & Gson",
                license = "Apache License 2.0",
                description = "Type-safe HTTP client for Android and Java."
            )
            
            LicenseItem(
                name = "Dagger Hilt",
                license = "Apache License 2.0",
                description = "Dependency injection library for Android."
            )
        }
    }
}

@Composable
fun LicenseItem(name: String, license: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = "License: $license", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
    }
}
