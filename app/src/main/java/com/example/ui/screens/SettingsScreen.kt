package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ApiKey
import com.example.domain.model.ProviderType
import com.example.ui.components.premium.DynamicGlassCard
import com.example.ui.components.premium.LiquidGlassSurface
import com.example.ui.components.premium.bounceClick
import com.example.ui.viewmodel.SettingsViewModel
import androidx.compose.foundation.clickable
import com.example.ui.viewmodel.SettingsUiState
import com.example.data.repository.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToModels: () -> Unit,
    onNavigateToCustomProviders: () -> Unit,
    onNavigateToAppearance: () -> Unit,
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Keys", "Behavior", "Appearance", "Advanced", "Stats")

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Column {
                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)
                        .statusBarsPadding(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.bounceClick { onBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(
                            "Settings Center", 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                        )
                    }
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
            when (selectedTabIndex) {
                0 -> { // Provider Keys
                    item {
                        Button(
                            onClick = onNavigateToModels,
                            modifier = Modifier.fillMaxWidth().height(56.dp).bounceClick { onNavigateToModels() },
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        ) { Text("Manage Models", style = MaterialTheme.typography.titleMedium) }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onNavigateToCustomProviders,
                            modifier = Modifier.fillMaxWidth().height(56.dp).bounceClick { onNavigateToCustomProviders() },
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f))
                        ) { Text("Manage Custom Endpoints", style = MaterialTheme.typography.titleMedium) }
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
                }
                1 -> { // App & Chat Behavior
                    item {
                        BehaviorSettingsSection(
                            settings = uiState.appSettings,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                    }
                }
                2 -> { // Appearance
                    item {
                        Button(
                            onClick = onNavigateToAppearance,
                            modifier = Modifier.fillMaxWidth().height(56.dp).bounceClick { onNavigateToAppearance() },
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f))
                        ) { Text("Themes & Appearance details", style = MaterialTheme.typography.titleMedium) }
                        Spacer(modifier = Modifier.height(16.dp))
                        AppearanceSettingsSection(
                            settings = uiState.appSettings,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                    }
                }
                3 -> { // Advanced, Agent, Search
                    item {
                        AdvancedWorkflowSettingsSection(
                            settings = uiState.appSettings,
                            onUpdate = { viewModel.updateSettings(it) }
                        )
                    }
                }
                4 -> { // Stats
                    item {
                        TokenStatsSection(uiState.tokenStats)
                    }
                }
            }
        }
    }
}

@Composable
fun AppearanceSettingsSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    Text("Appearance Tweaks", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
    DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
        Column {
            SettingsToggleItem("Compact Mode", settings.compactMode) { onUpdate { it.copy(compactMode = !it.compactMode) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Ambient Gradient", settings.enableAmbientGradient) { onUpdate { it.copy(enableAmbientGradient = !it.enableAmbientGradient) } }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Blur Intensity: ${(settings.blurIntensity * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(value = settings.blurIntensity, onValueChange = { v -> onUpdate { it.copy(blurIntensity = v) } })
                
                Text("Glow Intensity: ${(settings.glowIntensity * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(value = settings.glowIntensity, onValueChange = { v -> onUpdate { it.copy(glowIntensity = v) } })
                
                Text("Glass Transparency: ${(settings.glassTransparency * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(value = settings.glassTransparency, onValueChange = { v -> onUpdate { it.copy(glassTransparency = v) } })
                
                Text("Corner Radius Scale: ${(settings.scaleCornerRadius * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                Slider(value = settings.scaleCornerRadius, onValueChange = { v -> onUpdate { it.copy(scaleCornerRadius = v) } }, valueRange = 0.5f..2.0f)
            }
        }
    }
}

@Composable
fun BehaviorSettingsSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    Text("Chat Behavior", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
    DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
        Column {
            SettingsToggleItem("Auto-scroll", settings.autoScroll) { onUpdate { it.copy(autoScroll = !it.autoScroll) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Send on Enter", settings.sendOnEnter) { onUpdate { it.copy(sendOnEnter = !it.sendOnEnter) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Render Markdown", settings.renderMarkdown) { onUpdate { it.copy(renderMarkdown = !it.renderMarkdown) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Render HTML (Caution)", settings.renderHtml) { onUpdate { it.copy(renderHtml = !it.renderHtml) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Group Linked Messages", settings.groupMessages) { onUpdate { it.copy(groupMessages = !it.groupMessages) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Show Timestamps", settings.showTimestamps) { onUpdate { it.copy(showTimestamps = !it.showTimestamps) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Auto-follow stream", settings.autoFollowStream) { onUpdate { it.copy(autoFollowStream = !it.autoFollowStream) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Provider Auto-Failover", settings.autoFailover) { onUpdate { it.copy(autoFailover = !it.autoFailover) } }
        }
    }
}

@Composable
fun AdvancedWorkflowSettingsSection(settings: AppSettings, onUpdate: ((AppSettings) -> AppSettings) -> Unit) {
    Text("Workflow & Search", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
    DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
        Column {
            SettingsToggleItem("Deep Web Research", settings.deepResearchEnabled) { onUpdate { it.copy(deepResearchEnabled = !it.deepResearchEnabled) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Max Search Sources: ${settings.maxSources}", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = settings.maxSources.toFloat(), 
                    onValueChange = { v -> onUpdate { it.copy(maxSources = v.toInt()) } },
                    valueRange = 1f..15f,
                    steps = 14
                )
            }
        }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    Text("Diagnostics", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
    DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
        Column {
            SettingsToggleItem("Diagnostics Mode", settings.diagnosticsMode) { onUpdate { it.copy(diagnosticsMode = !it.diagnosticsMode) } }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            SettingsToggleItem("Network Logging", settings.networkLogging) { onUpdate { it.copy(networkLogging = !it.networkLogging) } }
        }
    }
}

@Composable
fun TokenStatsSection(stats: List<com.example.data.database.TokenStatsResult>) {
    Text("Token Usage Statistics", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(vertical = 8.dp))
    DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (stats.isEmpty()) {
                Text("No token usage data yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val maxTokens = stats.maxOfOrNull { it.totalTokens }?.toFloat()?.takeIf { it > 0 } ?: 1f
                stats.sortedByDescending { it.totalTokens }.forEach { stat ->
                    val progress = (stat.totalTokens.toFloat() / maxTokens).coerceIn(0f, 1f)
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stat.modelIdUsed, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            Text("%,d".format(stat.totalTokens), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.small)) {
                            Box(modifier = Modifier.fillMaxWidth(progress).height(8.dp).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small))
                        }
                    }
                }
            }
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
        Switch(checked = checked, onCheckedChange = { onToggle() }, modifier = Modifier.bounceClick { onToggle() })
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

    DynamicGlassCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large, elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(provider.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                IconButton(onClick = { showAddForm = !showAddForm }, modifier = Modifier.size(32.dp).bounceClick { showAddForm = !showAddForm }) {
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
                    TextButton(onClick = { onDelete(key.id) }, modifier = Modifier.bounceClick { onDelete(key.id) }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (showAddForm) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(value = labelInput, onValueChange = { labelInput = it }, label = { Text("Label (e.g. Work Key)") }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = keyInput, onValueChange = { keyInput = it }, label = { Text("API Key") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddForm = false }, modifier = Modifier.bounceClick { showAddForm = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            if (keyInput.isNotBlank()) {
                                onSave(labelInput.ifBlank { "Default Key" }, keyInput)
                                keyInput = ""
                                labelInput = ""
                                showAddForm = false
                            }
                        }) { Text("Save") }
                    }
                }
            }
        }
    }
}


