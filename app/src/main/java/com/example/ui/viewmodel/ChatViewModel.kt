package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.network.ChatStreamEvent
import com.example.repository.ChatRepository
import com.example.repository.ConversationRepository
import com.example.repository.ModelRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isStreaming: Boolean = false,
    val currentInput: String = "",
    val activeProvider: ProviderType = ProviderType.GROQ,
    val activeModelId: String = "",
    val availableProviders: List<ProviderType> = ProviderType.entries.toList(),
    val models: List<ProviderModel> = emptyList()
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var activeConversationId: Long? = null
    private var messagesJob: Job? = null
    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            modelRepository.seedModelsIfEmpty()
            
            // Collect models to update dropdown
            modelRepository.getAllModels().collect { models ->
                _uiState.update { it.copy(models = models) }
                val currentProvider = _uiState.value.activeProvider
                val currentModel = _uiState.value.activeModelId
                
                val modelExists = models.any { it.providerType == currentProvider && it.modelId == currentModel }
                if (!modelExists && models.isNotEmpty()) {
                    val defaultModel = models.firstOrNull { it.providerType == currentProvider && it.isDefault }
                        ?: models.firstOrNull { it.providerType == currentProvider }
                        ?: models.firstOrNull { it.isDefault } ?: models.first()
                    _uiState.update { 
                        it.copy(
                            activeProvider = defaultModel.providerType,
                            activeModelId = defaultModel.modelId
                        ) 
                    }
                }
            }
        }
    }

    fun loadConversation(id: Long) {
        if (activeConversationId == id) return
        activeConversationId = id
        messagesJob?.cancel()
        streamJob?.cancel()

        viewModelScope.launch {
            conversationRepository.getConversation(id).filterNotNull().collectLatest { conv ->
                _uiState.update { 
                    it.copy(
                        conversation = conv,
                        activeProvider = conv.selectedProvider,
                        activeModelId = conv.selectedModelId,
                        currentInput = conv.draftText
                    )
                }
            }
        }

        messagesJob = viewModelScope.launch {
            conversationRepository.getMessages(id).collect { msgs ->
                _uiState.update { it.copy(messages = msgs) }
            }
        }
    }

    fun startNewConversation() {
        activeConversationId = null
        messagesJob?.cancel()
        streamJob?.cancel()
        _uiState.update {
            it.copy(
                conversation = null,
                messages = emptyList(),
                currentInput = "",
                isStreaming = false
            )
        }
    }

    fun selectProviderModel(provider: ProviderType, modelId: String) {
        _uiState.update { it.copy(activeProvider = provider, activeModelId = modelId) }
        val conv = _uiState.value.conversation
        if (conv != null) {
            viewModelScope.launch {
                conversationRepository.updateConversation(conv.copy(selectedProvider = provider, selectedModelId = modelId))
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(currentInput = text) }
        val conv = _uiState.value.conversation
        if (conv != null) {
            viewModelScope.launch {
                conversationRepository.updateConversation(conv.copy(draftText = text))
            }
        }
    }

    fun sendMessage() {
        val input = _uiState.value.currentInput.trim()
        if (input.isEmpty() || _uiState.value.isStreaming) return

        val provider = _uiState.value.activeProvider
        val model = _uiState.value.activeModelId
        
        if (model.isEmpty()) return

        _uiState.update { it.copy(currentInput = "") }

        viewModelScope.launch {
            var convId = activeConversationId
            if (convId == null) {
                // Create new conversation
                val newConv = ChatConversation(
                    title = input.take(30) + if(input.length > 30) "..." else "",
                    selectedProvider = provider,
                    selectedModelId = model,
                    draftText = ""
                )
                convId = conversationRepository.createConversation(newConv)
                loadConversation(convId)
            } else {
                val conv = _uiState.value.conversation
                if (conv != null) {
                    conversationRepository.updateConversation(conv.copy(draftText = ""))
                }
            }

            // Insert User Message
            val userMsg = ChatMessage(conversationId = convId!!, role = MessageRole.USER, content = input)
            conversationRepository.insertMessage(userMsg)

            // Insert blank Assistant message for streaming
            val assistantMsg = ChatMessage(conversationId = convId, role = MessageRole.ASSISTANT, content = "", isStreaming = true)
            val assistantMsgId = conversationRepository.insertMessage(assistantMsg)

            streamResponse(convId, assistantMsgId, provider, model)
        }
    }

    private fun streamResponse(convId: Long, assistantMsgId: Long, provider: ProviderType, model: String) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            val history = _uiState.value.messages.filter { !it.isStreaming && it.errorMessage == null }
            
            _uiState.update { it.copy(isStreaming = true) }
            
            var currentContent = ""
            var errorMessage: String? = null
            var tokenCount: Int? = null
            val startTimeMs = System.currentTimeMillis()
            var endTimeMs: Long? = null

            chatRepository.sendMessageStream(convId, provider, model, history).collect { event ->
                when(event) {
                    is ChatStreamEvent.Started -> {}
                    is ChatStreamEvent.Delta -> {
                        currentContent += event.text
                        // In a real app we'd debounce DB updates, but for simplicity we update frequently
                        conversationRepository.updateMessage(
                            ChatMessage(id = assistantMsgId, conversationId = convId, role = MessageRole.ASSISTANT, content = currentContent, isStreaming = true)
                        )
                    }
                    is ChatStreamEvent.Completed -> {
                        currentContent = event.text
                        tokenCount = event.usageTokens
                        endTimeMs = System.currentTimeMillis()
                    }
                    is ChatStreamEvent.Error -> {
                        errorMessage = event.message
                        endTimeMs = System.currentTimeMillis()
                    }
                }
            }

            if (endTimeMs == null) endTimeMs = System.currentTimeMillis()
            val durationMs = endTimeMs!! - startTimeMs

            // Final update
            val finalMsg = ChatMessage(
                id = assistantMsgId,
                conversationId = convId,
                role = MessageRole.ASSISTANT,
                content = currentContent,
                isStreaming = false,
                errorMessage = errorMessage,
                generationTimeMs = durationMs,
                tokenCount = tokenCount,
                modelIdUsed = model
            )
            conversationRepository.updateMessage(finalMsg)
            _uiState.update { it.copy(isStreaming = false) }
        }
    }
    
    fun stopStreaming() {
        streamJob?.cancel()
        _uiState.update { it.copy(isStreaming = false) }
        // We should also find the currently streaming message in DB and mark it complete
        val streamingMsg = _uiState.value.messages.find { it.isStreaming }
        if (streamingMsg != null) {
            viewModelScope.launch {
                conversationRepository.updateMessage(streamingMsg.copy(isStreaming = false))
            }
        }
    }
}
