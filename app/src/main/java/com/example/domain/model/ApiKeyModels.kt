package com.example.domain.model

data class ApiKey(
    val id: String,
    val provider: ProviderType,
    val label: String,
    val isEnabled: Boolean = true,
    val isPreferred: Boolean = false,
    val addedAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long? = null,
    val failureCount: Int = 0
)

data class DiagnosticsRequest(
    val id: String,
    val provider: ProviderType,
    val model: String,
    val success: Boolean,
    val latencyMs: Long,
    val errorBody: String?,
    val timestamp: Long = System.currentTimeMillis()
)
