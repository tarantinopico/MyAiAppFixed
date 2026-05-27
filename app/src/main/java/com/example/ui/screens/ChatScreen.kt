package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageRole
import com.example.domain.model.ProviderType
import com.example.ui.components.premium.DynamicGlassCard
import com.example.ui.components.premium.LiquidGlassSurface
import com.example.ui.components.premium.bounceClick
import com.example.ui.viewmodel.ChatViewModel
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView

import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPromptLibrary: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    val scrollController = com.example.ui.components.rememberSmartAutoScrollState(
        listState = listState,
        itemCount = uiState.messages.size,
        isStreaming = uiState.isStreaming
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LiquidGlassSurface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            ProviderModelDropdown(
                                selectedProvider = uiState.activeProvider,
                                selectedModelId = uiState.activeModelId,
                                availableProviders = uiState.availableProviders,
                                models = uiState.models,
                                onSelectionChanged = { p, m -> viewModel.selectProviderModel(p, m) }
                            )
                        }

                        Row {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val exportManager = remember { com.example.data.storage.FileExportManager(context) }
                            
                            IconButton(onClick = { 
                                val title = uiState.conversation?.title ?: "Chat"
                                exportManager.shareConversation(title, uiState.messages)
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Export")
                            }
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(Icons.Default.Settings, contentDescription = "Settings")
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                if (uiState.messages.isEmpty()) {
                    Box(modifier = Modifier.weight(1f)) {
                        com.example.ui.components.EmptyChatState(
                            provider = uiState.activeProvider,
                            onSuggestionClick = { suggestion ->
                                viewModel.onInputChanged(suggestion)
                                viewModel.sendMessage()
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        contentPadding = PaddingValues(
                            top = 8.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { msg ->
                            ChatMessageItem(
                                message = msg,
                                onPreviewFile = { file -> 
                                    viewModel.setPreviewFile(file)
                                }
                            )
                        }
                    }
                }
                
                AdvancedChatComposer(
                    text = uiState.currentInput,
                    isStreaming = uiState.isStreaming,
                    isAgentModeEnabled = uiState.isAgentModeEnabled,
                    onAgentModeChanged = { viewModel.setAgentMode(it) },
                    isSearchModeEnabled = uiState.isSearchModeEnabled,
                    onSearchModeChanged = { viewModel.setSearchMode(it) },
                    onTextChanged = { viewModel.onInputChanged(it) },
                    onSend = { viewModel.sendMessage() },
                    onStop = { viewModel.stopStreaming() },
                    onNavigateToPromptLibrary = onNavigateToPromptLibrary
                )
            }
        }
        
        // Jump To Latest FAB
        androidx.compose.animation.AnimatedVisibility(
            visible = !scrollController.isAutoFollowEnabled && uiState.messages.isNotEmpty(),
            enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
            exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
        ) {
            SmallFloatingActionButton(
                onClick = { scrollController.jumpToBottom(uiState.messages.size) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(androidx.compose.material.icons.Icons.Default.KeyboardArrowDown, contentDescription = "Scroll to bottom")
            }
        }
        
        AnimatedVisibility(
            visible = uiState.selectedFileToPreview != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            uiState.selectedFileToPreview?.let { file ->
                BackHandler {
                    viewModel.setPreviewFile(null)
                }
                FilePreviewScreen(
                    file = file,
                    onBack = { viewModel.setPreviewFile(null) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderModelDropdown(
    selectedProvider: ProviderType,
    selectedModelId: String,
    availableProviders: List<ProviderType>,
    models: List<com.example.domain.model.ProviderModel>,
    onSelectionChanged: (ProviderType, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedModelName = models.find { it.providerType == selectedProvider && it.modelId == selectedModelId }?.displayName ?: selectedModelId

    Box {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${selectedProvider.name} • $selectedModelName",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            availableProviders.forEach { provider ->
                val providerModels = models.filter { it.providerType == provider }
                if (providerModels.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text(provider.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) },
                        onClick = { },
                        enabled = false
                    )
                    providerModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.displayName, style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                onSelectionChanged(provider, model.modelId)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ChatMessageItem(message: ChatMessage, onPreviewFile: (com.example.domain.model.GeneratedFile) -> Unit) {
    if (message.role == MessageRole.SYSTEM) {
        SystemMessageItem(message, onPreviewFile)
        return
    }

    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    val userGradient = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(com.example.ui.theme.GradientStart, com.example.ui.theme.GradientEnd)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = alignment
    ) {
        if (!isUser) {
            Row(
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("AI", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(message.modelIdUsed ?: "Assistant", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        val cardShape = RoundedCornerShape(
            topStart = if(isUser) 24.dp else 4.dp,
            topEnd = if(isUser) 4.dp else 24.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )

        if (isUser) {
            Surface(
                shape = cardShape,
                color = Color.Transparent,
                modifier = Modifier.widthIn(max = 340.dp).padding(end = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(userGradient, shape = cardShape)
                        .padding(1.dp) // Subtle border
                        .background(Color.White.copy(alpha = 0.05f), shape = cardShape)
                ) {
                    Column(
                        modifier = Modifier
                            .background(userGradient)
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = message.content,
                                color = textColor,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f).padding(bottom = 2.dp)
                            )
                            
                            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                            IconButton(
                                onClick = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content)) },
                                modifier = Modifier.size(24.dp).padding(start = 8.dp, top = 2.dp).bounceClick { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content)) }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = textColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            com.example.ui.components.premium.DynamicGlassCard(
                shape = cardShape,
                modifier = Modifier.widthIn(max = 340.dp),
                elevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
                ) {
                    if (message.errorMessage != null) {
                        Row(
                            modifier = Modifier.padding(bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Error generating response",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Text(
                            text = message.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else if (message.isStreaming && message.content.isEmpty()) {
                        com.example.ui.components.AnimatedTypingIndicator(modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        com.example.ui.components.PremiumMarkdownRenderer(
                            markdown = message.content,
                            textColor = textColor
                        )
                        
                        if (message.systemEvent != null && message.systemEvent.files.isNotEmpty()) {
                            if (message.content.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val exportManager = remember { com.example.data.storage.FileExportManager(context) }
                            
                            message.systemEvent.files.forEach { file ->
                                com.example.ui.components.GeneratedFileCard(
                                    file = file,
                                    onPreview = { onPreviewFile(file) },
                                    onDownload = { exportManager.shareFile(file) },
                                    onOpen = { exportManager.openFile(file) }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                    
                    // Metadata footer for assistant
                    if (message.errorMessage == null && !message.isStreaming && message.content.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content))
                                    },
                                modifier = Modifier.size(24.dp).bounceClick { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(message.content)) }
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            
                            val genTime = message.generationTimeMs?.let { "${String.format(java.util.Locale.US, "%.1f", it / 1000.0)}s" }
                            val tokens = message.tokenCount?.let { "$it tok" }
                            
                            val metaList = listOfNotNull(genTime, tokens)
                            if (metaList.isNotEmpty()) {
                                Text(
                                    text = metaList.joinToString(" • "),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemMessageItem(message: ChatMessage, onPreviewFile: (com.example.domain.model.GeneratedFile) -> Unit) {
    if (message.systemEvent != null) {
        com.example.ui.components.SystemEventItem(
            event = message.systemEvent, 
            onPreviewFile = onPreviewFile
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Stop, // Will replace with Info/Check later if needed
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

