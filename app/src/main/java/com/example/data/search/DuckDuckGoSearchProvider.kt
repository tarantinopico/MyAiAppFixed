package com.example.data.search

import com.example.domain.search.SearchProvider
import com.example.domain.search.SearchResult
import org.jsoup.Jsoup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DuckDuckGoSearchProvider : SearchProvider {
    override suspend fun search(query: String): List<SearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<SearchResult>()
        try {
            val url = "https://html.duckduckgo.com/html/?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            val doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(5000)
                .get()

            val elements = doc.select(".result")
            for (element in elements.take(5)) {
                val titleElem = element.select(".result__title > a.result__a").first()
                val snippetElem = element.select(".result__snippet").first()
                
                if (titleElem != null && snippetElem != null) {
                    var link = titleElem.attr("href")
                    // Handle DuckDuckGo redirect format if needed
                    if (link.startsWith("//duckduckgo.com/l/?uddg=")) {
                        val decoded = java.net.URLDecoder.decode(link.substringAfter("uddg=").substringBefore("&rut="), "UTF-8")
                        link = decoded
                    }
                    results.add(SearchResult(
                        title = titleElem.text(),
                        link = link,
                        snippet = snippetElem.text()
                    ))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        results
    }
}
