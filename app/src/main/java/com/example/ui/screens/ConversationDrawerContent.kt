package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.domain.model.ChatConversation
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.ConversationListViewModel
import com.example.ui.components.GlassCard

import com.example.ui.components.premium.DynamicGlassCard
import com.example.ui.components.premium.bounceClick

import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.MoreVert

@Composable
fun ConversationDrawerContent(
    navController: NavController,
    listViewModel: ConversationListViewModel,
    chatViewModel: ChatViewModel,
    onCloseDrawer: () -> Unit
) {
    val uiState by listViewModel.uiState.collectAsStateWithLifecycle()
    var showNewChatSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))) {
        // Redesigned Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome, 
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    ) // Placeholder for App Logo
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Operant AI", 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showNewChatSheet = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Chat", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Chat")
                    }
                }
            }
        }
        
        if (showNewChatSheet) {
            com.example.ui.components.NewChatCreationSheet(
                recentConversations = uiState.conversations,
                onDismiss = { showNewChatSheet = false },
                onCreateChat = { selectedContextIds, isAgentMode ->
                    showNewChatSheet = false
                    chatViewModel.startNewConversationWithContext(selectedContextIds, isAgentMode)
                    onCloseDrawer()
                }
            )
        }
        
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { listViewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent
            )
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
        ) {
            val pinned = uiState.conversations.filter { it.pinned }
            val recent = uiState.conversations.filter { !it.pinned }

            if (pinned.isNotEmpty()) {
                item {
                    Text(
                        "Pinned",
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(pinned, key = { "pinned-${it.id}" }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        onClick = {
                            chatViewModel.loadConversation(conv.id)
                            onCloseDrawer()
                        },
                        onTogglePin = { listViewModel.togglePin(conv) },
                        onDelete = { listViewModel.deleteConversation(conv.id) }
                    )
                }
            }

            if (recent.isNotEmpty()) {
                item {
                    Text(
                        "Recent",
                        modifier = Modifier.padding(start = 24.dp, bottom = 8.dp, top = 16.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(recent, key = { "recent-${it.id}" }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        onClick = {
                            chatViewModel.loadConversation(conv.id)
                            onCloseDrawer()
                        },
                        onTogglePin = { listViewModel.togglePin(conv) },
                        onDelete = { listViewModel.deleteConversation(conv.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    DynamicGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .bounceClick { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Outlined.Message, // Generic icon for chat
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp).padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title.ifEmpty { "New Conversation" },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${conversation.selectedProvider.name} • ${conversation.selectedModelId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp).bounceClick { showMenu = true }) {
                    Icon(
                        Icons.Default.MoreVert, 
                        contentDescription = "Options", 
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text(if (conversation.pinned) "Unpin" else "Pin") },
                        leadingIcon = { Icon(if (conversation.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin, contentDescription = null) },
                        onClick = { 
                            showMenu = false
                            onTogglePin()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { 
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}
