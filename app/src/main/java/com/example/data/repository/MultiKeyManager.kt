package com.example.data.repository

import com.example.data.database.ApiKeyDao
import com.example.data.database.ApiKeyEntity
import com.example.data.storage.SecureApiKeyStore
import com.example.domain.model.ApiKey
import com.example.domain.model.ProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import java.util.UUID

class MultiKeyManager(
    private val apiKeyDao: ApiKeyDao,
    private val keyStore: SecureApiKeyStore
) {
    fun getAllKeys(): Flow<List<ApiKey>> = apiKeyDao.getAllApiKeys().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveKey(
        provider: ProviderType,
        label: String,
        keyValue: String,
        isPreferred: Boolean = false
    ) {
        val id = UUID.randomUUID().toString()
        val entity = ApiKeyEntity(
            id = id,
            provider = provider,
            label = label,
            isEnabled = true,
            isPreferred = isPreferred,
            addedAt = System.currentTimeMillis(),
            lastUsedAt = null,
            failureCount = 0
        )
        // Save metadata to DB
        apiKeyDao.insertApiKey(entity)
        // Save actual key securely
        keyStore.saveApiKey(id, keyValue)
    }

    suspend fun updateKeyMetadata(key: ApiKey) {
        apiKeyDao.updateApiKey(key.toEntity())
    }

    suspend fun recordKeyUsage(id: String, success: Boolean) {
        val allEntities = apiKeyDao.getAllApiKeys().first()
        val target = allEntities.find { it.id == id } ?: return
        if (success) {
            apiKeyDao.updateApiKey(target.copy(lastUsedAt = System.currentTimeMillis(), failureCount = 0))
        } else {
            apiKeyDao.updateApiKey(target.copy(failureCount = target.failureCount + 1))
        }
    }

    suspend fun deleteKey(id: String) {
        val allEntities = apiKeyDao.getAllApiKeys().first()
        val target = allEntities.find { it.id == id } ?: return
        apiKeyDao.deleteApiKey(target)
        keyStore.deleteApiKey(id)
    }

    // Returns a Pair of the DB Entity ID and the actual Secure Key value.
    suspend fun getBestKeyForProvider(provider: ProviderType): Pair<String, String>? {
        val allEntities = apiKeyDao.getAllApiKeys().first()
        val providerKeys = allEntities.filter { it.provider == provider && it.isEnabled && it.failureCount < 3 }
        
        // prefer 'isPreferred', then 'lastUsedAt' ascending (least recently used)
        val selected = providerKeys.sortedWith(compareByDescending<ApiKeyEntity> { it.isPreferred }.thenBy { it.lastUsedAt }).firstOrNull()
        
        if (selected != null) {
            val keyValue = keyStore.getApiKey(selected.id)
            if (keyValue != null) return Pair(selected.id, keyValue)
        }
        return null
    }
}

fun ApiKeyEntity.toDomain() = ApiKey(id, provider, label, isEnabled, isPreferred, addedAt, lastUsedAt, failureCount)
fun ApiKey.toEntity() = ApiKeyEntity(id, provider, label, isEnabled, isPreferred, addedAt, lastUsedAt, failureCount)
