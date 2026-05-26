package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ProviderType
import com.example.domain.model.ProviderModel
import com.example.ui.viewmodel.ModelManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelManagementScreen(
    onBack: () -> Unit,
    viewModel: ModelManagementViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var newDisplayName by remember { mutableStateOf("") }
    var newModelId by remember { mutableStateOf("") }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                title = { Text("Manage Models") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ScrollableTabRow(
                selectedTabIndex = ProviderType.entries.indexOf(uiState.selectedProvider),
                edgePadding = 0.dp
            ) {
                ProviderType.entries.forEach { provider ->
                    Tab(
                        selected = uiState.selectedProvider == provider,
                        onClick = { viewModel.selectProvider(provider) },
                        text = { Text(provider.name) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Add New Custom Model", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newModelId,
                        onValueChange = { newModelId = it },
                        label = { Text("Model ID") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            viewModel.addModel(newDisplayName, newModelId, false)
                            newDisplayName = ""
                            newModelId = ""
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(uiState.models, key = { it.id }) { model ->
                    ModelItem(
                        model = model,
                        onDelete = { viewModel.deleteModelForce(model) },
                        onSetDefault = { viewModel.setDefault(model) }
                    )
                }
            }
        }
    }
}

@Composable
fun ModelItem(
    model: ProviderModel,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(model.displayName, style = MaterialTheme.typography.titleMedium)
                Text(model.modelId, style = MaterialTheme.typography.bodySmall)
                if (model.isSeeded) {
                    Text("Default Seeded Model", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            IconButton(onClick = onSetDefault) {
                Icon(
                    imageVector = if (model.isDefault) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Default",
                    tint = if (model.isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
