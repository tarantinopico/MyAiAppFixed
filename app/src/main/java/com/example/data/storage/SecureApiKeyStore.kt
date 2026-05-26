package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.domain.model.ProviderType

class SecureApiKeyStore(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "api_keys_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(providerType: ProviderType, apiKey: String) {
        sharedPrefs.edit().putString(providerType.name, apiKey).apply()
    }

    fun getApiKey(providerType: ProviderType): String? {
        return sharedPrefs.getString(providerType.name, null)?.takeIf { it.isNotBlank() }
    }

    fun deleteApiKey(providerType: ProviderType) {
        sharedPrefs.edit().remove(providerType.name).apply()
    }

    fun hasApiKey(providerType: ProviderType): Boolean {
        return !sharedPrefs.getString(providerType.name, null).isNullOrBlank()
    }
}
