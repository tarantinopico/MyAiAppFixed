package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.domain.model.ProviderType
import java.util.UUID

class SecureApiKeyStore(
    private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "api_keys_prefs_v2",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(id: String, apiKey: String) {
        sharedPrefs.edit().putString(id, apiKey.trim()).apply()
    }

    fun getApiKey(id: String): String? {
        return sharedPrefs.getString(id, null)?.trim()?.takeIf { it.isNotBlank() }
    }

    fun deleteApiKey(id: String) {
        sharedPrefs.edit().remove(id).apply()
    }
}
