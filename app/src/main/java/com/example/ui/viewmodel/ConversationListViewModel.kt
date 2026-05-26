package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ChatConversation
import com.example.repository.ConversationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ConversationListUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val isViewingArchived: Boolean = false,
    val searchQuery: String = ""
)

class ConversationListViewModel(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.flatMapLatest { state ->
                if (state.searchQuery.isNotBlank()) {
                    conversationRepository.searchConversations(state.searchQuery)
                        .map { list -> list.filter { it.archived == state.isViewingArchived } }
                } else {
                    conversationRepository.getRecentConversations(state.isViewingArchived)
                }
            }.collect { list ->
                _uiState.update { it.copy(conversations = list) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleArchivedView() {
        _uiState.update { it.copy(isViewingArchived = !it.isViewingArchived, searchQuery = "") }
    }

    fun togglePin(conversation: ChatConversation) {
        viewModelScope.launch {
            conversationRepository.updateConversation(conversation.copy(pinned = !conversation.pinned))
        }
    }

    fun toggleArchive(conversation: ChatConversation) {
        viewModelScope.launch {
            conversationRepository.updateConversation(conversation.copy(archived = !conversation.archived))
        }
    }

    fun renameConversation(conversation: ChatConversation, newTitle: String) {
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                conversationRepository.updateConversation(conversation.copy(title = newTitle.trim()))
            }
        }
    }

    fun deleteConversation(id: Long) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(id)
        }
    }
}
