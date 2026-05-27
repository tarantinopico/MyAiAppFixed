package com.example.repository

import com.example.data.database.ModelDao
import com.example.data.database.ProviderModelEntity
import com.example.domain.model.ProviderModel
import com.example.domain.model.ProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ModelRepository(
    private val modelDao: ModelDao
) {
    fun getAllModels(): Flow<List<ProviderModel>> {
        return modelDao.getAllModels().map { list -> list.map { it.toDomain() } }
    }

    fun getModelsByProvider(providerType: ProviderType): Flow<List<ProviderModel>> {
        return modelDao.getModelsByProvider(providerType.name).map { list -> list.map { it.toDomain() } }
    }

    suspend fun insertModel(model: ProviderModel) {
        if (model.isDefault) {
            modelDao.clearDefaults(model.providerType.name)
        }
        modelDao.insertModel(model.toEntity())
    }

    suspend fun deleteModel(model: ProviderModel) {
        modelDao.deleteModel(model.toEntity())
    }

    suspend fun seedModelsIfEmpty() {
        if (modelDao.getSeededModelCount() == 0) {
            val defaults = listOf(
                // Groq
                ProviderModelEntity(0, ProviderType.GROQ, "Llama 3.1 8B", "llama-3.1-8b-instant", true, 0, true, 8192, false, false, true),
                ProviderModelEntity(0, ProviderType.GROQ, "Llama 3.3 70B", "llama-3.3-70b-versatile", false, 1, true, 8192, false, false, true),
                ProviderModelEntity(0, ProviderType.GROQ, "Mixtral 8x7B", "mixtral-8x7b-32768", false, 2, true, 32768, false, false, true),
                
                // Gemini
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 1.5 Flash", "gemini-1.5-flash", true, 0, true, 1048576, false, true, true),
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 1.5 Pro", "gemini-1.5-pro", false, 1, true, 2097152, false, true, true),
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 1.5 Flash-8B", "gemini-1.5-flash-8b", false, 2, true, 1048576, false, true, true),
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 2.0 Flash", "gemini-2.0-flash", false, 3, true, 1048576, false, true, true),
                
                // Cerebras
                ProviderModelEntity(0, ProviderType.CEREBRAS, "Llama 3.1 8B", "llama3.1-8b", true, 0, true, 8192, false, false, false),
                ProviderModelEntity(0, ProviderType.CEREBRAS, "Llama 3.1 70B", "llama3.1-70b", false, 1, true, 8192, false, false, false),
                
                // OpenAI
                ProviderModelEntity(0, ProviderType.OPENAI, "GPT-4o", "gpt-4o", true, 0, true, 128000, false, true, true),
                ProviderModelEntity(0, ProviderType.OPENAI, "GPT-4o Mini", "gpt-4o-mini", false, 1, true, 128000, false, true, true),
                ProviderModelEntity(0, ProviderType.OPENAI, "o1", "o1", false, 2, true, 200000, true, true, false),
                ProviderModelEntity(0, ProviderType.OPENAI, "o3-mini", "o3-mini", false, 3, true, 200000, true, false, true),
                
                // Anthropic
                ProviderModelEntity(0, ProviderType.ANTHROPIC, "Claude 3.5 Sonnet", "claude-3-5-sonnet-latest", true, 0, true, 200000, false, true, true),
                ProviderModelEntity(0, ProviderType.ANTHROPIC, "Claude 3.5 Haiku", "claude-3-5-haiku-latest", false, 1, true, 200000, false, true, true),
                ProviderModelEntity(0, ProviderType.ANTHROPIC, "Claude 3 Opus", "claude-3-opus-latest", false, 2, true, 200000, false, true, true)
            )
            modelDao.insertModels(defaults)
        }
    }

    private fun ProviderModelEntity.toDomain() = ProviderModel(
        id = id,
        providerType = providerType,
        displayName = displayName,
        modelId = modelId,
        isDefault = isDefault,
        sortOrder = sortOrder,
        isSeeded = isSeeded,
        contextLength = contextLength,
        isReasoning = isReasoning,
        isVision = isVision,
        supportsTools = supportsTools
    )

    private fun ProviderModel.toEntity() = ProviderModelEntity(
        id = id,
        providerType = providerType,
        displayName = displayName,
        modelId = modelId,
        isDefault = isDefault,
        sortOrder = sortOrder,
        isSeeded = isSeeded,
        contextLength = contextLength,
        isReasoning = isReasoning,
        isVision = isVision,
        supportsTools = supportsTools
    )
}
