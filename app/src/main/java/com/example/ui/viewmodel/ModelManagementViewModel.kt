package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ProviderModel
import com.example.domain.model.ProviderType
import com.example.repository.ModelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModelManagementUiState(
    val selectedProvider: ProviderType = ProviderType.GROQ,
    val models: List<ProviderModel> = emptyList()
)

class ModelManagementViewModel(
    private val modelRepository: ModelRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModelManagementUiState())
    val uiState: StateFlow<ModelManagementUiState> = _uiState.asStateFlow()

    init {
        loadModels()
    }

    fun selectProvider(providerType: ProviderType) {
        _uiState.update { it.copy(selectedProvider = providerType) }
        loadModels()
    }

    private fun loadModels() {
        viewModelScope.launch {
            modelRepository.getModelsByProvider(_uiState.value.selectedProvider).collect { list ->
                _uiState.update { it.copy(models = list) }
            }
        }
    }

    fun addModel(displayName: String, modelId: String, isDefault: Boolean) {
        if (displayName.isBlank() || modelId.isBlank()) return
        val provider = _uiState.value.selectedProvider
        val newModel = ProviderModel(
            providerType = provider,
            displayName = displayName.trim(),
            modelId = modelId.trim(),
            isDefault = isDefault,
            sortOrder = _uiState.value.models.size,
            isSeeded = false
        )
        viewModelScope.launch {
            modelRepository.insertModel(newModel)
        }
    }

    fun deleteModel(model: ProviderModel) {
        if (!model.isSeeded) { // Ensure we don't delete seed models unless we want to allow it
            viewModelScope.launch {
                modelRepository.deleteModel(model)
            }
        }
    }
    
    fun deleteModelForce(model: ProviderModel) {
       viewModelScope.launch {
            modelRepository.deleteModel(model)
        }
    }

    fun setDefault(model: ProviderModel) {
        viewModelScope.launch {
            modelRepository.insertModel(model.copy(isDefault = true))
        }
    }
}
