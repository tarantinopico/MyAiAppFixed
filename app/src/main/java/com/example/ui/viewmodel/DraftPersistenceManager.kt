package com.example.ui.viewmodel

import com.example.repository.ConversationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class DraftPersistenceManager(
    private val conversationRepository: ConversationRepository,
    private val scope: CoroutineScope
) {
    private var draftJob: Job? = null
    
    fun saveDraft(conversationId: Long?, text: String) {
        if (conversationId == null) return
        draftJob?.cancel()
        draftJob = scope.launch {
            delay(500) // Debounce
            val conv = conversationRepository.getConversation(conversationId).firstOrNull()
            if (conv != null && conv.draftText != text) {
                conversationRepository.updateConversation(conv.copy(draftText = text))
            }
        }
    }
}
