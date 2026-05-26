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
    val choices: List<Choice>?
)

@JsonClass(generateAdapter = true)
data class Choice(
    val index: Int?,
    val message: NetworkMessage?,
    val delta: NetworkMessage?, // for streaming
    val finish_reason: String?
)
