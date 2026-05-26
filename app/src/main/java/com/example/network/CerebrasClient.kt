package com.example.network

import com.example.domain.model.MessageRole
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.buffer
import java.io.IOException

class CerebrasClient(
    private val api: CerebrasApi,
    private val moshi: Moshi
) : LlmProviderClient {

    override suspend fun streamChatCompletion(
        apiKey: String,
        modelId: String,
        messages: List<InternalMessageDTO>,
        systemPrompt: String?
    ): Flow<ChatStreamEvent> = flow {
        emit(ChatStreamEvent.Started)

        val networkMessages = mutableListOf<NetworkMessage>()
        if (!systemPrompt.isNullOrBlank()) {
            networkMessages.add(NetworkMessage("system", systemPrompt))
        }
        networkMessages.addAll(messages.map {
            NetworkMessage(
                role = when (it.role) {
                    MessageRole.USER -> "user"
                    MessageRole.ASSISTANT -> "assistant"
                    MessageRole.SYSTEM -> "system"
                },
                content = it.content
            )
        })

        val request = ChatCompletionRequest(
            model = modelId,
            messages = networkMessages,
            stream = true
        )

        try {
            val response = api.createChatCompletionStream("Bearer $apiKey", request)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                val parsedError = try {
                    val mapAdapter = moshi.adapter(Map::class.java)
                    val map = mapAdapter.fromJson(errorBody) as? Map<*, *>
                    val errorObj = map?.get("error") as? Map<*, *>
                    errorObj?.get("message")?.toString()
                } catch (e: Exception) { null }
                
                val errorMessage = parsedError ?: "HTTP ${response.code()}: $errorBody"
                emit(ChatStreamEvent.Error(errorMessage))
                return@flow
            }

            val body = response.body() ?: throw IOException("Empty response body")
            val source = body.source()

            val adapter = moshi.adapter(ChatCompletionResponse::class.java)
            var fullText = ""
            var totalTokens: Int? = null

            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    try {
                        val parsed = adapter.fromJson(data)
                        val deltaText = parsed?.choices?.firstOrNull()?.delta?.content
                        if (deltaText != null) {
                            fullText += deltaText
                            emit(ChatStreamEvent.Delta(deltaText))
                        }
                        if (parsed?.usage?.total_tokens != null) {
                            totalTokens = parsed.usage.total_tokens
                        }
                    } catch (e: Exception) {
                        // ignore parse errors
                    }
                }
            }
            emit(ChatStreamEvent.Completed(fullText, totalTokens))

        } catch (e: Exception) {
            emit(ChatStreamEvent.Error(e.message ?: "Unknown streaming error", e))
        }
    }.flowOn(Dispatchers.IO)
}
