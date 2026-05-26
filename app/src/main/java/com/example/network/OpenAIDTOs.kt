package com.example.network

import com.example.domain.model.ProviderType
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    val model: String,
    val messages: List<NetworkMessage>,
    val stream: Boolean = false,
    val temperature: Double? = null,
    val max_tokens: Int? = null
)

@JsonClass(generateAdapter = true)
data class NetworkMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    val id: String?,
    val choices: List<Choice>?,
    val usage: Usage? = null,
    val x_groq: GroqExtensions? = null
)

@JsonClass(generateAdapter = true)
data class GroqExtensions(
    val usage: Usage?
)

@JsonClass(generateAdapter = true)
data class Usage(
    val prompt_tokens: Int?,
    val completion_tokens: Int?,
    val total_tokens: Int?
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int?,
    val message: NetworkMessage?,
    val delta: DeltaMessage?, // for streaming
    val finish_reason: String?
)

@JsonClass(generateAdapter = true)
data class DeltaMessage(
    val role: String?,
    val content: String?
)
