package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.Prompt
import com.example.data.repository.PromptPreferences
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PromptLibraryViewModel(
    private val promptPreferences: PromptPreferences
) : ViewModel() {
    
    val prompts: StateFlow<List<Prompt>> = promptPreferences.prompts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun savePrompt(prompt: Prompt) {
        promptPreferences.savePrompt(prompt)
    }

    fun deletePrompt(id: String) {
        promptPreferences.deletePrompt(id)
    }
}
