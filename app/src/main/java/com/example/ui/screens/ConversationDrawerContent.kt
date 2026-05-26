package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Conversations") },
            actions = {
                IconButton(onClick = {
                    chatViewModel.startNewConversation()
                    onCloseDrawer()
                }) {
                    Icon(Icons.Default.Add, contentDescription = "New Chat")
                }
            }
        )
        
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { listViewModel.onSearchQueryChanged(it) },
            placeholder = { Text("Search...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp)
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
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.title.ifEmpty { "New Conversation" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${conversation.selectedProvider} • ${conversation.selectedModelId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
