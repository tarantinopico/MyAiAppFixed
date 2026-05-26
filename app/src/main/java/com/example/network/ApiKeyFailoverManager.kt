package com.example.network

import com.example.data.repository.MultiKeyManager
import com.example.domain.model.ProviderType
import java.io.IOException

class ApiKeyFailoverManager(
    private val keyManager: MultiKeyManager
) {
    suspend fun <T> executeWithFailover(
        provider: ProviderType,
        onSystemMessage: (String) -> Unit = {},
        action: suspend (apiKey: String) -> T
    ): Pair<T, String> { // Returns the result and the ID of the key that worked (or was tried)
        var lastException: Exception? = null
        
        // Let's get up to 3 keys, one by one until one works
        for (i in 0 until 3) {
            val keyInfo = keyManager.getBestKeyForProvider(provider)
                ?: throw IOException("No available API Keys for $provider. Please configure keys in Settings.")
            
            val (keyId, keyValue) = keyInfo
            
            if (i > 0) {
                onSystemMessage("Retrying request... using alternative $provider key.")
            }

            try {
                val result = action(keyValue)
                keyManager.recordKeyUsage(keyId, true)
                return Pair(result, keyId)
            } catch (e: Exception) {
                lastException = e
                keyManager.recordKeyUsage(keyId, false)
                // If it's a known non-retryable error, maybe break? 
                // For now, retry with another key due to rate limits or key quota exhaustion
                onSystemMessage("Request failed (${e.message}). Falling back...")
            }
        }
        
        throw IOException("All API keys failed for $provider", lastException)
    }
}
