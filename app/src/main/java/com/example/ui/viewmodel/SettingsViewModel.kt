package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ProviderType
import com.example.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ApiKeyStatus(
    val provider: ProviderType,
    val hasKey: Boolean
)

data class SettingsUiState(
    val apiKeysStatus: List<ApiKeyStatus> = emptyList()
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshStatuses()
    }

    fun refreshStatuses() {
        val statuses = ProviderType.entries.map { provider ->
            ApiKeyStatus(provider, settingsRepository.hasApiKey(provider))
        }
        _uiState.update { it.copy(apiKeysStatus = statuses) }
    }

    fun saveApiKey(provider: ProviderType, key: String) {
        if (key.isNotBlank()) {
            settingsRepository.saveApiKey(provider, key.trim())
            refreshStatuses()
        }
    }

    fun deleteApiKey(provider: ProviderType) {
        settingsRepository.deleteApiKey(provider)
        refreshStatuses()
    }
}
