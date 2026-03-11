package com.aegisnet.ui.firewall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aegisnet.database.entity.AppDomainRule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainRuleEditor(
    appUid: Int,
    onNavigateBack: () -> Unit,
    viewModel: DomainRuleViewModel = hiltViewModel()
) {
    LaunchedEffect(appUid) {
        viewModel.loadRules(appUid)
    }

    val rules by viewModel.rules.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Domain Rules") },
                navigationIcon = {
                    Button(onClick = onNavigateBack) { Text("Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Rule")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(rules) { rule ->
                RuleListItem(
                    rule = rule,
                    onDelete = { viewModel.deleteRule(rule) }
                )
            }
        }

        if (showAddDialog) {
            AddRuleDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { domain, action, matchType ->
                    viewModel.addRule(appUid, domain, action, matchType)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun RuleListItem(rule: AppDomainRule, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = rule.domain, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${rule.action} - ${rule.matchType}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (rule.action == "BLOCK") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Rule")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var domain by remember { mutableStateOf("") }
    var action by remember { mutableStateOf("BLOCK") }
    var matchType by remember { mutableStateOf("SUFFIX") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Domain Rule") },
        text = {
            Column {
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain (e.g., ads.example.com)") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Action")
                Row {
                    FilterChip(selected = action == "BLOCK", onClick = { action = "BLOCK" }, label = { Text("Block") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = action == "ALLOW", onClick = { action = "ALLOW" }, label = { Text("Allow") })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Match Type")
                Row {
                    FilterChip(selected = matchType == "SUFFIX", onClick = { matchType = "SUFFIX" }, label = { Text("Suffix") })
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(selected = matchType == "EXACT", onClick = { matchType = "EXACT" }, label = { Text("Exact") })
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(domain, action, matchType) },
                enabled = domain.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
