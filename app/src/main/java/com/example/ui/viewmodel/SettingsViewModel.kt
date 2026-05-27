package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ProviderType
import com.example.domain.model.ApiKey
import com.example.repository.SettingsRepository
import com.example.repository.ConversationRepository
import com.example.data.database.TokenStatsResult
import com.example.data.repository.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKeys: List<ApiKey> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val tokenStats: List<TokenStatsResult> = emptyList()
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getAllApiKeys().collect { keys ->
                _uiState.update { it.copy(apiKeys = keys) }
            }
        }
        viewModelScope.launch {
            conversationRepository.getTokenStatsByModel().collect { stats ->
                _uiState.update { it.copy(tokenStats = stats) }
            }
        }
        viewModelScope.launch {
            settingsRepository.appPreferences.settings.collect { appSettings ->
                _uiState.update { it.copy(appSettings = appSettings) }
            }
        }
    }

    fun updateSettings(updater: (AppSettings) -> AppSettings) {
        settingsRepository.appPreferences.updateSettings(updater)
    }

    fun saveApiKey(provider: ProviderType, key: String, label: String = "Default Key") {
        if (key.isNotBlank()) {
            viewModelScope.launch {
                settingsRepository.saveApiKey(provider, key.trim(), label)
            }
        }
    }

    fun deleteApiKey(id: String) {
        viewModelScope.launch {
            settingsRepository.deleteApiKey(id)
        }
    }
}

