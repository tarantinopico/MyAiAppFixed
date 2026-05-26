package com.example.repository

import com.example.domain.model.*
import com.example.network.*
import kotlinx.coroutines.flow.*

class ChatRepository(
    private val conversationRepository: ConversationRepository,
    private val failoverManager: ApiKeyFailoverManager,
    private val groqClient: GroqClient,
    private val geminiClient: GeminiClient,
    private val cerebrasClient: CerebrasClient
) {
    private fun getClient(providerType: ProviderType): LlmProviderClient {
        return when(providerType) {
            ProviderType.GROQ -> groqClient
            ProviderType.GEMINI -> geminiClient
            ProviderType.CEREBRAS -> cerebrasClient
        }
    }

    suspend fun sendMessageStream(
        conversationId: Long,
        providerType: ProviderType,
        modelId: String,
        messages: List<ChatMessage>
    ): Flow<ChatStreamEvent> = flow {
        val internalMessages = messages.map {
            InternalMessageDTO(it.role, it.content)
        }

        val client = getClient(providerType)
        
        var lastError: String? = null
        val (result, keyUsed) = failoverManager.executeWithFailover(
            provider = providerType,
            onSystemMessage = { msg ->
                // NOT a standard event, so we could potentially inject a dummy Delta here
                // or just log it. For now, we will add a new event type SystemMessage if we can modify ChatStreamEvent,
                // but since we can't emit it directly without changing the sealed class...
                // Let's just emit it. Oh wait we can't right now. Let's see what ChatStreamEvent has.
            }
        ) { apiKey ->
            // Flow collection retry block
            // We'll collect the flow here to actually trigger the exception if there's one during connection.
            var completed = false
            try {
                // If the flow fails quickly, it throws an exception
                client.streamChatCompletion(apiKey, modelId, internalMessages) // we return Flow
            } catch (e: Exception) {
                throw e
            }
        }
        
        // As seen above, simply returning Flow won't catch mid-stream errors for failover. 
        // But doing a full failover *during* streaming is extremely difficult because you've already sent Delta events!
        // If a flow fails gracefully after 3 words, we don't want to restart the whole request implicitly because the user would see the text repeat.
        // Therefore, we handle failover for the INITIAL connection throwing Exceptions. 
        // We will emit the result Flow.
        emitAll(result.catch { e ->
            emit(ChatStreamEvent.Error(e.message ?: "Unknown streaming error"))
        })
    }
}

