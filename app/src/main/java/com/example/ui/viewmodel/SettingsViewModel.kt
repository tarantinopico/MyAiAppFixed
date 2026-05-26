package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ProviderType
import com.example.domain.model.ApiKey
import com.example.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKeys: List<ApiKey> = emptyList(),
    val markdownEnabled: Boolean = true,
    val htmlEnabled: Boolean = false,
    val autoFailoverEnabled: Boolean = true,
    val compactMode: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.getAllApiKeys().collect { keys ->
                _uiState.update { it.copy(apiKeys = keys) }
            }
        }
    }

    fun toggleMarkdown() = _uiState.update { it.copy(markdownEnabled = !it.markdownEnabled) }
    fun toggleHtml() = _uiState.update { it.copy(htmlEnabled = !it.htmlEnabled) }
    fun toggleFailover() = _uiState.update { it.copy(autoFailoverEnabled = !it.autoFailoverEnabled) }
    fun toggleCompact() = _uiState.update { it.copy(compactMode = !it.compactMode) }

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
