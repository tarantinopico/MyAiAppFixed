package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ApiKey
import com.example.domain.model.ProviderType
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSurface
import com.example.ui.viewmodel.SettingsViewModel
import androidx.compose.foundation.clickable
import com.example.ui.viewmodel.SettingsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToModels: () -> Unit,
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        "Settings", 
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding() + 8.dp,
                bottom = innerPadding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "API Keys", 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            items(ProviderType.entries) { provider ->
                val providerKeys = uiState.apiKeys.filter { it.provider == provider }
                ProviderKeyManagerCard(
                    provider = provider,
                    keys = providerKeys,
                    onSave = { label, key -> viewModel.saveApiKey(provider, key, label) },
                    onDelete = { id -> viewModel.deleteApiKey(id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Models", 
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onNavigateToModels,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                ) {
                    Text("Manage Provider Models", style = MaterialTheme.typography.titleMedium)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                AdvancedSettingsSection(
                    uiState = uiState,
                    onToggleMarkdown = viewModel::toggleMarkdown,
                    onToggleHtml = viewModel::toggleHtml,
                    onToggleFailover = viewModel::toggleFailover,
                    onToggleCompact = viewModel::toggleCompact
                )
            }
        }
    }
}

@Composable
fun AdvancedSettingsSection(
    uiState: SettingsUiState,
    onToggleMarkdown: () -> Unit,
    onToggleHtml: () -> Unit,
    onToggleFailover: () -> Unit,
    onToggleCompact: () -> Unit
) {
    Text(
        text = "Advanced Settings",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = 2.dp
    ) {
        Column {
            SettingsToggleItem("Render Markdown", uiState.markdownEnabled, onToggleMarkdown)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Render HTML (Caution)", uiState.htmlEnabled, onToggleHtml)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Enable Provider Failover", uiState.autoFailoverEnabled, onToggleFailover)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Compact Mode", uiState.compactMode, onToggleCompact)
        }
    }
}

@Composable
fun SettingsToggleItem(title: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = { onToggle() })
    }
}

@Composable
fun ProviderKeyManagerCard(
    provider: ProviderType,
    keys: List<ApiKey>,
    onSave: (String, String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showAddForm by remember { mutableStateOf(false) }
    var keyInput by remember { mutableStateOf("") }
    var labelInput by remember { mutableStateOf("") }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = provider.name, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { showAddForm = !showAddForm }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add Key", tint = MaterialTheme.colorScheme.primary)
                }
            }
            
            if (keys.isEmpty() && !showAddForm) {
                Text("No keys configured", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            }

            keys.forEach { key ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(key.label, style = MaterialTheme.typography.bodyLarge)
                            if (key.isPreferred) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.CheckCircle, contentDescription = "Preferred", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(if (key.failureCount > 0) "${key.failureCount} Failures" else "Healthy", style = MaterialTheme.typography.labelSmall, color = if(key.failureCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = { onDelete(key.id) }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (showAddForm) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(
                        value = labelInput,
                        onValueChange = { labelInput = it },
                        label = { Text("Label (e.g. Work Key)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("API Key") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddForm = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (keyInput.isNotBlank()) {
                                onSave(labelInput.ifBlank { "Default Key" }, keyInput)
                                keyInput = ""
                                labelInput = ""
                                showAddForm = false
                            }
                        }) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

