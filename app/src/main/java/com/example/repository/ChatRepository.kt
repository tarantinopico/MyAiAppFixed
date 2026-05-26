package com.example.repository

import com.example.domain.model.*
import com.example.network.*
import kotlinx.coroutines.flow.*

class ChatRepository(
    private val conversationRepository: ConversationRepository,
    private val settingsRepository: SettingsRepository,
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
        val apiKey = settingsRepository.getApiKey(providerType)
        if (apiKey.isNullOrBlank()) {
            emit(ChatStreamEvent.Error("API Key missing or unauthorized for \$providerType"))
            return@flow
        }

        val internalMessages = messages.map {
            InternalMessageDTO(it.role, it.content)
        }

        val client = getClient(providerType)
        emitAll(client.streamChatCompletion(apiKey, modelId, internalMessages))
    }
}
