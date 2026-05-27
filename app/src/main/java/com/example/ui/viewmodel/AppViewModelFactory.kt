package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.AIModelAggregatorApplication
import com.example.repository.ChatRepository
import com.example.repository.ConversationRepository
import com.example.repository.ModelRepository
import com.example.repository.SettingsRepository

class AppViewModelFactory(
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository,
    private val modelRepository: ModelRepository,
    private val settingsRepository: SettingsRepository,
    private val sessionRestoreManager: com.example.repository.SessionRestoreManager,
    private val webSearchManager: com.example.domain.search.WebSearchManager,
    private val customProviderRepository: com.example.repository.CustomProviderRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(chatRepository, conversationRepository, modelRepository, sessionRestoreManager, webSearchManager) as T
            }
            modelClass.isAssignableFrom(ConversationListViewModel::class.java) -> {
                ConversationListViewModel(conversationRepository) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(settingsRepository, conversationRepository) as T
            }
            modelClass.isAssignableFrom(ModelManagementViewModel::class.java) -> {
                ModelManagementViewModel(modelRepository) as T
            }
            modelClass.isAssignableFrom(CustomProvidersViewModel::class.java) -> {
                CustomProvidersViewModel(customProviderRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
