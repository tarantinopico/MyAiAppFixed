package com.example.repository

import com.example.data.storage.SecureApiKeyStore
import com.example.domain.model.ProviderType

class SettingsRepository(
    private val keyStore: SecureApiKeyStore
) {
    fun saveApiKey(providerType: ProviderType, apiKey: String) {
        keyStore.saveApiKey(providerType, apiKey)
    }

    fun getApiKey(providerType: ProviderType): String? {
        return keyStore.getApiKey(providerType)
    }

    fun deleteApiKey(providerType: ProviderType) {
        keyStore.deleteApiKey(providerType)
    }

    fun hasApiKey(providerType: ProviderType): Boolean {
        return keyStore.hasApiKey(providerType)
    }
}
