package com.example.domain.model

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
    val errorMessage: String? = null
)
