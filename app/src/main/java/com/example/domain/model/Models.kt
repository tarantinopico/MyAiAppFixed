package com.example.domain.model

import com.squareup.moshi.JsonClass

data class ProviderModel(
    val id: Long = 0,
    val providerType: ProviderType,
    val displayName: String,
    val modelId: String,
    val isDefault: Boolean,
    val sortOrder: Int,
    val isSeeded: Boolean = false
)

data class ChatConversation(
    val id: Long = 0,
    val title: String,
    val selectedProvider: ProviderType,
    val selectedModelId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val draftText: String = ""
)

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class ChatMessage(
    val id: Long = 0,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val errorMessage: String? = null,
    val generationTimeMs: Long? = null,
    val tokenCount: Int? = null,
    val modelIdUsed: String? = null,
    val systemEvent: SystemEvent? = null
)

@JsonClass(generateAdapter = true)
data class GeneratedFile(
    val id: String,
    val name: String,
    val format: String,
    val content: String,
    val path: String? = null
)

enum class EventType {
    INFO, TOOL_CALL, FILE_GENERATION, WORKFLOW_START, WORKFLOW_END, ERROR, SEARCH_START, SEARCH_RESULT, SEARCH_END
}

@JsonClass(generateAdapter = true)
data class SystemEvent(
    val type: EventType,
    val message: String,
    val files: List<GeneratedFile> = emptyList()
)
