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
    val models: List<ProviderModel> = emptyList(),
    val isAgentModeEnabled: Boolean = false,
    val isSearchModeEnabled: Boolean = false,
    val isPlanModeEnabled: Boolean = false,
    val isLoopModeEnabled: Boolean = false,
    val selectedSkillId: String? = null,
    val selectedFileToPreview: GeneratedFile? = null,
    val planSteps: List<com.example.data.database.PlanStepEntity> = emptyList()
)

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val modelRepository: ModelRepository,
    private val sessionRestoreManager: com.example.repository.SessionRestoreManager,
    private val webSearchManager: com.example.domain.search.WebSearchManager,
    private val planStepDao: com.example.data.database.PlanStepDao
) : ViewModel() {

    private val draftPersistenceManager = DraftPersistenceManager(conversationRepository, viewModelScope)

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var activeConversationId: Long? = null
    private var messagesJob: Job? = null
    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            modelRepository.seedModelsIfEmpty()
            
            // Check session restore
            val lastId = sessionRestoreManager.lastActiveConversationId.value
            if (lastId != null) {
                loadConversation(lastId)
            }

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

    private var planStepsJob: Job? = null

    fun loadConversation(id: Long) {
        if (activeConversationId == id) return
        activeConversationId = id
        sessionRestoreManager.setLastActiveConversation(id)
        messagesJob?.cancel()
        streamJob?.cancel()
        planStepsJob?.cancel()

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
        
        planStepsJob = viewModelScope.launch {
            planStepDao.getPlanSteps(id).collect { steps ->
                _uiState.update { it.copy(planSteps = steps) }
            }
        }
    }

    fun startNewConversation() {
        activeConversationId = null
        sessionRestoreManager.setLastActiveConversation(null)
        messagesJob?.cancel()
        streamJob?.cancel()
        planStepsJob?.cancel()
        _uiState.update {
            it.copy(
                conversation = null,
                messages = emptyList(),
                currentInput = "",
                isStreaming = false,
                planSteps = emptyList()
            )
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun startNewConversationWithContext(contextIds: List<Long>, isAgentMode: Boolean) {
        startNewConversation()
        setAgentMode(isAgentMode)
        if (contextIds.isNotEmpty()) {
            viewModelScope.launch {
                val contextMessages = java.lang.StringBuilder()
                for (id in contextIds) {
                    val messages = conversationRepository.getMessages(id).firstOrNull()
                    val conv = conversationRepository.getConversation(id).firstOrNull()
                    if (conv != null && messages != null) {
                        contextMessages.append("Context from conversation '${conv.title}':\n")
                        messages.forEach {
                            if (it.role == MessageRole.USER || it.role == MessageRole.ASSISTANT) {
                                contextMessages.append("${it.role.name}: ${it.content}\n")
                            }
                        }
                        contextMessages.append("\n")
                    }
                }
                if (contextMessages.isNotEmpty()) {
                    val newConv = ChatConversation(
                        title = "New Contextual Conversation",
                        selectedProvider = _uiState.value.activeProvider,
                        selectedModelId = _uiState.value.activeModelId,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    val newConvId = conversationRepository.createConversation(newConv)
                    activeConversationId = newConvId
                    
                    val sysMsg = ChatMessage(
                        conversationId = newConvId,
                        role = MessageRole.SYSTEM,
                        content = "Injected context from previous conversations.\n\n$contextMessages"
                    )
                    conversationRepository.insertMessage(sysMsg)
                    loadConversation(newConvId)
                }
            }
        }
    }

    fun updateConversationTitle(newTitle: String) {
        val conv = _uiState.value.conversation
        if (conv != null) {
            viewModelScope.launch {
                conversationRepository.updateConversation(conv.copy(title = newTitle))
                loadConversation(conv.id)
            }
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

    fun setAgentMode(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                isAgentModeEnabled = enabled, 
                isSearchModeEnabled = if (enabled) false else it.isSearchModeEnabled 
            ) 
        }
    }

    fun setSearchMode(enabled: Boolean) {
        _uiState.update { 
            it.copy(
                isSearchModeEnabled = enabled, 
                isAgentModeEnabled = if (enabled) false else it.isAgentModeEnabled 
            ) 
        }
    }

    fun setPlanMode(enabled: Boolean) {
        _uiState.update { it.copy(isPlanModeEnabled = enabled) }
    }
    
    fun setLoopMode(enabled: Boolean) {
        _uiState.update { it.copy(isLoopModeEnabled = enabled) }
    }
    
    fun setSelectedSkill(skillId: String?) {
        _uiState.update { it.copy(selectedSkillId = skillId) }
    }
    
    fun setPreviewFile(file: GeneratedFile?) {
        _uiState.update { it.copy(selectedFileToPreview = file) }
    }

    fun executePlan() {
        val convId = activeConversationId ?: return
        val steps = _uiState.value.planSteps.filter { it.status == "PENDING" || it.status == "FAILED" }.sortedBy { it.stepIndex }
        if (steps.isEmpty()) return
        
        val provider = _uiState.value.activeProvider
        val model = _uiState.value.activeModelId
        
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            _uiState.update { it.copy(isStreaming = true) }
            
            for (step in steps) {
                // Update specific step to IN_PROGRESS
                planStepDao.updatePlanStep(step.copy(status = "IN_PROGRESS"))
                
                val assistantMsg = ChatMessage(conversationId = convId, role = MessageRole.ASSISTANT, content = "Executing Step: ${step.title}...", isStreaming = true)
                val assistantMsgId = conversationRepository.insertMessage(assistantMsg)
                
                try {
                    val messages = conversationRepository.getMessagesSync(convId)
                    val historyWithContext = mutableListOf<ChatMessage>()
                    
                    val skillId = _uiState.value.selectedSkillId
                    if (skillId != null) {
                        // Normally we would look up the skill using SkillDao, for now just append simple text
                        historyWithContext.add(ChatMessage(conversationId = 0, role = MessageRole.SYSTEM, content = "You are using an AI skill ID: $skillId"))
                    }
                    
                    historyWithContext.addAll(messages)
                    historyWithContext.add(ChatMessage(conversationId = 0, role = MessageRole.USER, content = "Execute the following step of our plan: ${step.title}. Assume preceding steps are done. Present your response clearly."))
                    
                    var fullResponse = ""
                    chatRepository.sendMessageStream(convId, provider, model, historyWithContext, false).collect { event ->
                        when(event) {
                            is com.example.network.ChatStreamEvent.Delta -> {
                                fullResponse += event.text
                                val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                                if (assistant != null) {
                                    conversationRepository.updateMessage(assistant.copy(content = fullResponse))
                                }
                            }
                            else -> {}
                        }
                    }
                    
                    // Finished
                    val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                    if (assistant != null) {
                        conversationRepository.updateMessage(assistant.copy(isStreaming = false))
                    }
                    
                    planStepDao.updatePlanStep(step.copy(status = "COMPLETED"))
                    
                } catch (e: Exception) {
                    val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                    if (assistant != null) {
                        conversationRepository.updateMessage(assistant.copy(content = "Error executing step '${step.title}': ${e.message}", isStreaming = false, errorMessage = e.message))
                    }
                    planStepDao.updatePlanStep(step.copy(status = "FAILED"))
                    break // halt execution of plan
                }
            }
            _uiState.update { it.copy(isStreaming = false) }
            loadConversation(convId)
        }
    }

    fun cancelPlan() {
        val convId = activeConversationId ?: return
        viewModelScope.launch {
            val steps = _uiState.value.planSteps.filter { it.status == "PENDING" || it.status == "IN_PROGRESS" }
            for (step in steps) {
                planStepDao.updatePlanStep(step.copy(status = "FAILED"))
            }
            streamJob?.cancel()
            _uiState.update { it.copy(isStreaming = false) }
            loadConversation(convId)
        }
    }

    fun retryPlanStep(stepId: Long) {
        viewModelScope.launch {
            val step = _uiState.value.planSteps.find { it.id == stepId }
            if (step != null) {
                planStepDao.updatePlanStep(step.copy(status = "PENDING"))
                executePlan()
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(currentInput = text) }
        draftPersistenceManager.saveDraft(activeConversationId, text)
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

            if (_uiState.value.isPlanModeEnabled) {
                _uiState.update { it.copy(isPlanModeEnabled = false, isStreaming = true) }
                val planPrompt = "You are an AI autonomous agent planner. Analyze the following request and return ONLY a bulleted list of high-level plan steps required to complete it. Do not include introductory text, just the steps starting with '-', e.g. '- Set up project structure'. Request: $input"
                streamJob = viewModelScope.launch {
                    val messages = listOf(ChatMessage(conversationId = 0, role = MessageRole.USER, content = planPrompt))
                    var fullResponse = ""
                    try {
                        chatRepository.sendMessageStream(convId, provider, model, messages, false).collect { event ->
                            when (event) {
                                is com.example.network.ChatStreamEvent.Delta -> {
                                    fullResponse += event.text
                                    val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                                    if (assistant != null) {
                                        conversationRepository.updateMessage(assistant.copy(content = fullResponse))
                                    }
                                }
                                else -> {}
                            }
                        }
                        
                        // Parse list into plan steps
                        val steps = fullResponse.lines().filter { it.trim().startsWith("-") || it.trim().matches(Regex("^\\d+\\..*")) }
                            .map { it.trim().removePrefix("-").replace(Regex("^\\d+\\.\\s*"), "").trim() }
                        
                        if (steps.isNotEmpty()) {
                            steps.forEachIndexed { index, title ->
                                planStepDao.insertPlanStep(
                                    com.example.data.database.PlanStepEntity(
                                        id = 0, // AutoGenerate
                                        conversationId = convId,
                                        title = title,
                                        description = "Pending execution step.",
                                        status = "PENDING",
                                        stepIndex = index,
                                        resultText = null,
                                        createdAt = System.currentTimeMillis()
                                    )
                                )
                            }
                            // replace the planner explanation with a short message
                            val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                            if (assistant != null) {
                                conversationRepository.updateMessage(assistant.copy(
                                    content = "I have generated an execution plan for your request. Please review the pending steps above.", 
                                    isStreaming = false
                                ))
                            }
                        } else {
                            val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                            if (assistant != null) {
                                conversationRepository.updateMessage(assistant.copy(isStreaming = false))
                            }
                        }
                        
                    } catch (e: Exception) {
                        val assistant = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                        if (assistant != null) {
                            conversationRepository.updateMessage(assistant.copy(content = "Error generating plan: ${e.message}", isStreaming = false, errorMessage = e.message))
                        }
                    } finally {
                        _uiState.update { it.copy(isStreaming = false) }
                        loadConversation(convId)
                    }
                }
                return@launch
            }

            if (_uiState.value.isSearchModeEnabled) {
                _uiState.update { it.copy(isSearchModeEnabled = false, isStreaming = true) }
                
                val eventsJob = viewModelScope.launch {
                    try {
                        webSearchManager.searchEvents.collect { event ->
                            if (event != null) {
                                val currentAssistantMsg = conversationRepository.getMessagesSync(convId).find { it.id == assistantMsgId }
                                if (currentAssistantMsg != null) {
                                    conversationRepository.updateMessage(currentAssistantMsg.copy(systemEvent = event))
                                    loadConversation(convId)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Do the actual search
                val searchContext = try {
                    webSearchManager.performResearch(input)
                } catch (e: Exception) {
                    "Error performing search: ${e.message}"
                }
                eventsJob.cancel()
                
                // Now insert the silent search context as a system message so the LLM knows
                val systemContextMsg = ChatMessage(
                    conversationId = convId,
                    role = MessageRole.SYSTEM,
                    content = searchContext
                )
                conversationRepository.insertMessage(systemContextMsg)
                
                streamResponse(convId, assistantMsgId, provider, model)
                
            } else {
                streamResponse(convId, assistantMsgId, provider, model)
            }
        }
    }

    private fun streamResponse(convId: Long, assistantMsgId: Long, provider: ProviderType, model: String) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            val history = conversationRepository.getMessagesSync(convId).filter { !it.isStreaming && it.errorMessage == null }
            val isAgentMode = _uiState.value.isAgentModeEnabled
            
            _uiState.update { it.copy(isStreaming = true) }
            
            var currentContent = ""
            var errorMessage: String? = null
            var tokenCount: Int? = null
            val startTimeMs = System.currentTimeMillis()
            var endTimeMs: Long? = null
            var systemEventToSave: SystemEvent? = null

            chatRepository.sendMessageStream(convId, provider, model, history, isAgentMode).collect { event ->
                when(event) {
                    is ChatStreamEvent.Started -> {}
                    is ChatStreamEvent.Delta -> {
                        currentContent += event.text
                        // Check for complete files
                        if (isAgentMode && currentContent.contains("</file>")) {
                            parseFiles(currentContent)?.let { files ->
                                systemEventToSave = SystemEvent(
                                    type = EventType.FILE_GENERATION,
                                    message = "Agent generated ${files.size} file(s)",
                                    files = files
                                )
                            }
                        }
                        
                        conversationRepository.updateMessage(
                            ChatMessage(id = assistantMsgId, conversationId = convId, role = MessageRole.ASSISTANT, content = cleanupContent(currentContent), isStreaming = true, systemEvent = systemEventToSave)
                        )
                    }
                    is ChatStreamEvent.Completed -> {
                        currentContent = event.text
                        if (isAgentMode) {
                             parseFiles(currentContent)?.let { files ->
                                systemEventToSave = SystemEvent(
                                    type = EventType.FILE_GENERATION,
                                    message = "Agent generated ${files.size} file(s)",
                                    files = files
                                )
                            }
                        }
                        tokenCount = event.usageTokens
                        endTimeMs = System.currentTimeMillis()
                    }
                    is ChatStreamEvent.SystemMessage -> {
                        val sysMsg = ChatMessage(conversationId = convId, role = MessageRole.SYSTEM, content = event.message)
                        conversationRepository.insertMessage(sysMsg)
                    }
                    is ChatStreamEvent.AgentEvent -> {
                        val sysMsg = ChatMessage(conversationId = convId, role = MessageRole.SYSTEM, content = event.systemEvent.message, systemEvent = event.systemEvent)
                        conversationRepository.insertMessage(sysMsg)
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
                content = cleanupContent(currentContent),
                isStreaming = false,
                errorMessage = errorMessage,
                generationTimeMs = durationMs,
                tokenCount = tokenCount,
                modelIdUsed = model,
                systemEvent = systemEventToSave
            )
            conversationRepository.updateMessage(finalMsg)
            _uiState.update { it.copy(isStreaming = false) }
        }
    }
    
    private fun parseFiles(content: String): List<GeneratedFile>? {
        val fileRegex = "<file\\s+name=\"([^\"]+)\">(.*?)</file>".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matches = fileRegex.findAll(content)
        val files = matches.mapIndexed { index, matchResult ->
            val name = matchResult.groupValues[1]
            val code = matchResult.groupValues[2].trim()
            val format = name.substringAfterLast('.', "")
            GeneratedFile(
                id = "file-$index",
                name = name,
                format = format,
                content = code
            )
        }.toList()
        return if (files.isNotEmpty()) files else null
    }
    
    private fun cleanupContent(content: String): String {
        return content.replace("<file\\s+name=\"([^\"]+)\">(.*?)</file>".toRegex(RegexOption.DOT_MATCHES_ALL), "").trim()
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
