package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ProviderType
import com.example.domain.model.MessageRole

@Entity(tableName = "provider_models")
data class ProviderModelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerType: ProviderType,
    val displayName: String,
    val modelId: String,
    val isDefault: Boolean,
    val sortOrder: Int,
    val isSeeded: Boolean,
    val contextLength: Int? = null,
    val isReasoning: Boolean = false,
    val isVision: Boolean = false,
    val supportsTools: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val selectedProvider: ProviderType,
    val selectedModelId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean,
    val archived: Boolean,
    val draftText: String
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val role: MessageRole,
    val content: String,
    val createdAt: Long,
    val isStreaming: Boolean,
    val errorMessage: String?,
    val generationTimeMs: Long? = null,
    val tokenCount: Int? = null,
    val modelIdUsed: String? = null,
    val systemEventJson: String? = null
)

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey val id: String,
    val provider: ProviderType,
    val label: String,
    val isEnabled: Boolean,
    val isPreferred: Boolean,
    val addedAt: Long,
    val lastUsedAt: Long?,
    val failureCount: Int
)

@Entity(tableName = "custom_providers")
data class CustomProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val isEnabled: Boolean = true,
    val sortOrder: Int = 0
)

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val providerType: ProviderType,
    val modelId: String,
    val systemPrompt: String,
    val temperature: Float = 0.7f,
    val sortOrder: Int = 0,
    val iconColorHex: String? = null
)

data class TokenStatsResult(
    val modelIdUsed: String,
    val totalTokens: Long
)
