package com.example.network

import kotlinx.coroutines.flow.Flow
import com.example.domain.model.MessageRole

sealed class ChatStreamEvent {
    object Started : ChatStreamEvent()
    data class Delta(val text: String) : ChatStreamEvent()
    data class Completed(val text: String, val usageTokens: Int? = null) : ChatStreamEvent()
    data class Error(val message: String, val throwable: Throwable? = null) : ChatStreamEvent()
    data class SystemMessage(val message: String) : ChatStreamEvent()
    data class AgentEvent(val systemEvent: com.example.domain.model.SystemEvent) : ChatStreamEvent()
}

data class InternalMessageDTO(
    val role: MessageRole,
    val content: String
)

interface LlmProviderClient {
    suspend fun streamChatCompletion(
        apiKey: String,
        modelId: String,
        messages: List<InternalMessageDTO>,
        systemPrompt: String? = null
    ): Flow<ChatStreamEvent>
}
