package com.example.repository

import com.example.domain.model.*
import com.example.network.*
import kotlinx.coroutines.flow.*

class ChatRepository(
    private val conversationRepository: ConversationRepository,
    private val failoverManager: ApiKeyFailoverManager,
    private val customProviderRepository: com.example.repository.CustomProviderRepository,
    private val moshi: com.squareup.moshi.Moshi,
    private val genericApi: com.example.network.GenericOpenAiApi,
    private val groqClient: GroqClient,
    private val geminiClient: GeminiClient,
    private val cerebrasClient: CerebrasClient,
    private val openAiClient: com.example.network.GenericOpenAiCompatibleClient,
    private val anthropicClient: com.example.network.GenericOpenAiCompatibleClient,
    private val openRouterClient: com.example.network.GenericOpenAiCompatibleClient,
    private val mistralClient: com.example.network.GenericOpenAiCompatibleClient,
    private val deepSeekClient: com.example.network.GenericOpenAiCompatibleClient,
    private val togetherClient: com.example.network.GenericOpenAiCompatibleClient,
    private val ollamaClient: com.example.network.GenericOpenAiCompatibleClient,
    private val localClient: com.example.network.GenericOpenAiCompatibleClient
) {
    private suspend fun getClient(providerType: ProviderType): LlmProviderClient {
        return when(providerType) {
            ProviderType.GROQ -> groqClient
            ProviderType.GEMINI -> geminiClient
            ProviderType.CEREBRAS -> cerebrasClient
            ProviderType.OPENAI -> openAiClient
            ProviderType.ANTHROPIC -> anthropicClient
            ProviderType.OPENROUTER -> openRouterClient
            ProviderType.MISTRAL -> mistralClient
            ProviderType.DEEPSEEK -> deepSeekClient
            ProviderType.TOGETHER -> togetherClient
            ProviderType.OLLAMA -> ollamaClient
            ProviderType.LOCAL -> localClient
            ProviderType.CUSTOM -> {
                // Read from DB and create dynamic client
                val allCustom = customProviderRepository.getAllCustomProviders().first()
                val custom = allCustom.firstOrNull() 
                if (custom != null) {
                    com.example.network.GenericOpenAiCompatibleClient(genericApi, moshi, custom.baseUrl)
                } else {
                    openAiClient
                }
            }
        }
    }

    suspend fun sendMessageStream(
        conversationId: Long,
        providerType: ProviderType,
        modelId: String,
        messages: List<ChatMessage>,
        isAgentMode: Boolean = false
    ): Flow<ChatStreamEvent> = flow {
        val internalMessages = messages.map {
            InternalMessageDTO(it.role, it.content)
        }.toMutableList()
        
        var prompt: String? = null
        if (isAgentMode) {
            prompt = """
            You are operating in Agent Mode within a premium AI workspace.
            You can generate file capabilities.
            When a user asks you to create or output code for a file, output exactly in this format:
            <file name="filename.ext">
            ...content exactly...
            </file>
            Any text outside is rendered as standard message context. You may output multiple files.
            """.trimIndent()
        }

        val client = getClient(providerType)
        
        try {
            var lastError: String? = null
            val (result, keyUsed) = failoverManager.executeWithFailover(
                provider = providerType,
                onSystemMessage = { msg ->
                    // Need to figure out how to emit system message later if needed
                }
            ) { apiKey ->
                // Flow collection retry block
                var completed = false
                try {
                    client.streamChatCompletion(apiKey, modelId, internalMessages, prompt)
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
        } catch (e: Exception) {
            emit(ChatStreamEvent.Error(e.message ?: "Failed to send message: ${e.message}"))
        }
    }
}

