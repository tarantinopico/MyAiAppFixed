package com.example.domain.search

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.domain.model.SystemEvent
import com.example.domain.model.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WebSearchManager(
    private val searchProvider: SearchProvider
) {
    private val _searchEvents = MutableStateFlow<SystemEvent?>(null)
    val searchEvents: StateFlow<SystemEvent?> = _searchEvents.asStateFlow()

    suspend fun performResearch(query: String): String = withContext(Dispatchers.IO) {
        _searchEvents.value = SystemEvent(EventType.SEARCH_START, "Searching the internet for '$query'...")
        
        val results = searchProvider.search(query)
        
        if (results.isEmpty()) {
            _searchEvents.value = SystemEvent(EventType.ERROR, "No search results found.")
            return@withContext "No information found on the internet regarding '$query'."
        }

        _searchEvents.value = SystemEvent(EventType.SEARCH_RESULT, "Found ${results.size} sources.")
        
        val contextBuilder = java.lang.StringBuilder()
        contextBuilder.append("Current Search Results:\n")
        results.forEachIndexed { index, result ->
            contextBuilder.append("[${index + 1}] Title: ${result.title}\n")
            contextBuilder.append("Link: ${result.link}\n")
            contextBuilder.append("Snippet: ${result.snippet}\n\n")
        }

        _searchEvents.value = SystemEvent(
            type = EventType.SEARCH_RESULT, 
            message = "Found ${results.size} sources.\n\n" + results.joinToString("\n") { "- [${it.title}](${it.link})" }
        )
        
        contextBuilder.toString()
    }
}
