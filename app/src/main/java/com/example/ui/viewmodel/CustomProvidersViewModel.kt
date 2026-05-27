package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.CustomProviderEntity
import com.example.repository.CustomProviderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CustomProvidersViewModel(private val repository: CustomProviderRepository) : ViewModel() {
    private val _providers = MutableStateFlow<List<CustomProviderEntity>>(emptyList())
    val providers: StateFlow<List<CustomProviderEntity>> = _providers.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllCustomProviders().collect { 
                _providers.value = it 
            }
        }
    }

    fun addProvider(provider: CustomProviderEntity) {
        viewModelScope.launch {
            repository.insertCustomProvider(provider)
        }
    }

    fun deleteProvider(provider: CustomProviderEntity) {
        viewModelScope.launch {
            repository.deleteCustomProvider(provider)
        }
    }
}
