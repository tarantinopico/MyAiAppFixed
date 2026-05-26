package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.domain.model.ChatMessage
import com.example.domain.model.MessageRole
import com.example.domain.model.ProviderType
import com.example.ui.viewmodel.ChatViewModel
import io.noties.markwon.Markwon
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.TextView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenDrawer: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size, uiState.isStreaming) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ProviderModelDropdown(
                        selectedProvider = uiState.activeProvider,
                        selectedModelId = uiState.activeModelId,
                        availableProviders = uiState.availableProviders,
                        models = uiState.models,
                        onSelectionChanged = { p, m -> viewModel.selectProviderModel(p, m) }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        bottomBar = {
            ChatComposer(
                text = uiState.currentInput,
                isStreaming = uiState.isStreaming,
                onTextChanged = { viewModel.onInputChanged(it) },
                onSend = { viewModel.sendMessage() },
                onStop = { viewModel.stopStreaming() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { msg ->
                    ChatMessageItem(message = msg)
                }
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = "\${selectedProvider.name} - \$selectedModelId",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableProviders.forEach { provider ->
                val providerModels = models.filter { it.providerType == provider }
                if (providerModels.isNotEmpty()) {
                    DropdownMenuItem(
                        text = { Text(provider.name, style = MaterialTheme.typography.labelSmall) },
                        onClick = { },
                        enabled = false
                    )
                    providerModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model.displayName) },
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
fun ChatComposer(
    text: String,
    isStreaming: Boolean,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text("Message AI...") },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(24.dp),
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            if (isStreaming) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop",
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = text.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .padding(12.dp)
                .widthIn(max = 300.dp)
        ) {
            if (message.errorMessage != null) {
                Text(
                    text = message.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                if (isUser) {
                    Text(text = message.content, color = textColor)
                } else {
                    MarkdownText(text = message.content, color = textColor)
                }
            }
        }
    }
}

@Composable
fun MarkdownText(text: String, color: androidx.compose.ui.graphics.Color) {
    AndroidView(
        factory = { context ->
            TextView(context).apply {
                setTextColor(color.toArgb())
            }
        },
        update = { textView ->
            val markwon = Markwon.create(textView.context)
            markwon.setMarkdown(textView, text)
        }
    )
}
