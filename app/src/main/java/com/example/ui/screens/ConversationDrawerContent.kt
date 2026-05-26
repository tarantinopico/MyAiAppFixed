package com.example.ui.screens

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDrawerContent(
    navController: NavController,
    listViewModel: ConversationListViewModel,
    chatViewModel: ChatViewModel,
    onCloseDrawer: () -> Unit
) {
    val uiState by listViewModel.uiState.collectAsStateWithLifecycle()
    var showNewChatSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 24.dp, bottom = 12.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("History", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = {
                showNewChatSheet = true
            }) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = "New Chat",
                    tint = MaterialTheme.colorScheme.primary
                )
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
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(20.dp)) },
            shape = CircleShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .height(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent
            )
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp)
        ) {
            items(uiState.conversations, key = { it.id }) { conv ->
                ConversationItem(
                    conversation = conv,
                    onClick = {
                        chatViewModel.loadConversation(conv.id)
                        onCloseDrawer()
                    },
                    onDelete = { listViewModel.deleteConversation(conv.id) }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: ChatConversation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title.ifEmpty { "New Conversation" },
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${conversation.selectedProvider.name} • ${conversation.selectedModelId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Delete", 
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
