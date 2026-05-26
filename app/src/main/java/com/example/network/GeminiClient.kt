package com.example.network

import com.example.domain.model.MessageRole
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okio.buffer
import java.io.IOException

class GeminiClient(
    private val api: GeminiApi,
    private val moshi: Moshi
) : LlmProviderClient {

    override suspend fun streamChatCompletion(
        apiKey: String,
        modelId: String,
        messages: List<InternalMessageDTO>,
        systemPrompt: String?
    ): Flow<ChatStreamEvent> = flow {
        emit(ChatStreamEvent.Started)

        val contents = messages.map {
            GeminiContent(
                role = if (it.role == MessageRole.USER) "user" else "model",
                parts = listOf(GeminiPart(it.content))
            )
        }

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemPrompt?.let { 
                GeminiContent(role = "user", parts = listOf(GeminiPart(it)))
            }
        )

        try {
            val response = api.streamGenerateContent(modelId, apiKey, request)
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

            val adapter = moshi.adapter(GeminiResponse::class.java)
            var fullText = ""

            // Gemini SSE alt=sse uses data: lines
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (line.startsWith("data: ")) {
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") break

                    try {
                        val parsed = adapter.fromJson(data)
                        val deltaText = parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (deltaText != null) {
                            fullText += deltaText
                            emit(ChatStreamEvent.Delta(deltaText))
                        }
                    } catch (e: Exception) {
                        // ignore parse errors
                    }
                }
            }
            emit(ChatStreamEvent.Completed(fullText))

        } catch (e: Exception) {
            emit(ChatStreamEvent.Error(e.message ?: "Unknown streaming error", e))
        }
    }.flowOn(Dispatchers.IO)
}
