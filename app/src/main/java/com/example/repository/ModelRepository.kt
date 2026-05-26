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
                ProviderModelEntity(0, ProviderType.GROQ, "Llama 3 8B", "llama3-8b-8192", true, 0, true),
                ProviderModelEntity(0, ProviderType.GROQ, "Llama 3 70B", "llama3-70b-8192", false, 1, true),
                ProviderModelEntity(0, ProviderType.GROQ, "Mixtral 8x7B", "mixtral-8x7b-32768", false, 2, true),
                
                // Gemini
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 1.5 Flash", "gemini-1.5-flash", true, 0, true),
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 1.5 Pro", "gemini-1.5-pro", false, 1, true),
                ProviderModelEntity(0, ProviderType.GEMINI, "Gemini 1.5 Flash-8B", "gemini-1.5-flash-8b", false, 2, true),
                
                // Cerebras
                ProviderModelEntity(0, ProviderType.CEREBRAS, "Llama 3.1 8B", "llama3.1-8b", true, 0, true),
                ProviderModelEntity(0, ProviderType.CEREBRAS, "Llama 3.1 70B", "llama3.1-70b", false, 1, true)
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
        isSeeded = isSeeded
    )

    private fun ProviderModel.toEntity() = ProviderModelEntity(
        id = id,
        providerType = providerType,
        displayName = displayName,
        modelId = modelId,
        isDefault = isDefault,
        sortOrder = sortOrder,
        isSeeded = isSeeded
    )
}
