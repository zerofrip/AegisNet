package com.aegisnet.ui.filter.http

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aegisnet.filter.http.HttpRule
import com.aegisnet.filter.http.parser.AdGuardHttpParser

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRuleEditor(
    onRuleAdded: (HttpRule) -> Unit,
    onBack: () -> Unit
) {
    var ruleText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val parser = remember { AdGuardHttpParser() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Custom Rule") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
            OutlinedTextField(
                value = ruleText,
                onValueChange = {
                    ruleText = it
                    errorMessage = null
                },
                label = { Text("Rule (e.g. ||ads.example.com^)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null,
                supportingText = {
                    if (errorMessage != null) {
                        Text(errorMessage!!)
                    } else {
                        Text("Supports AdGuard syntax")
                    }
                }
            )

            Button(
                onClick = {
                    val rule = parser.parse(ruleText)
                    if (rule != null) {
                        onRuleAdded(rule)
                        ruleText = ""
                        onBack()
                    } else {
                        errorMessage = "Invalid rule format"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Rule")
            }
        }
    }
}
