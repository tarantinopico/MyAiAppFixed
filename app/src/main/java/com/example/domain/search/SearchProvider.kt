package com.example.domain.search

data class SearchResult(
    val title: String,
    val link: String,
    val snippet: String
)

interface SearchProvider {
    suspend fun search(query: String): List<SearchResult>
}
