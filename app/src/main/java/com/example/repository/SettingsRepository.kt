package com.example.repository

import com.example.data.repository.MultiKeyManager
import com.example.domain.model.ApiKey
import com.example.domain.model.ProviderType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val keyManager: MultiKeyManager,
    val appPreferences: com.example.data.repository.AppPreferences
) {
    suspend fun saveApiKey(providerType: ProviderType, apiKey: String, label: String = "Default Key") {
        keyManager.saveKey(providerType, label, apiKey, isPreferred = true)
    }

    suspend fun deleteApiKey(id: String) {
        keyManager.deleteKey(id)
    }

    fun getAllApiKeys(): Flow<List<ApiKey>> {
        return keyManager.getAllKeys()
    }
}
