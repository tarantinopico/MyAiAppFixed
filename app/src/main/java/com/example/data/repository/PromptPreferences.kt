package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class Prompt(val id: String, val title: String, val text: String)

class PromptPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("prompt_prefs", Context.MODE_PRIVATE)
    
    private val _prompts = MutableStateFlow<List<Prompt>>(emptyList())
    val prompts: StateFlow<List<Prompt>> = _prompts.asStateFlow()

    init {
        loadPrompts()
    }

    private fun loadPrompts() {
        val jsonString = prefs.getString("prompts_json", "[]") ?: "[]"
        val jsonArray = JSONArray(jsonString)
        val loadedPrompts = mutableListOf<Prompt>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            loadedPrompts.add(Prompt(obj.getString("id"), obj.getString("title"), obj.getString("text")))
        }
        _prompts.value = loadedPrompts
    }

    fun savePrompt(prompt: Prompt) {
        val current = _prompts.value.toMutableList()
        val index = current.indexOfFirst { it.id == prompt.id }
        if (index != -1) {
            current[index] = prompt
        } else {
            current.add(prompt)
        }
        saveToPrefs(current)
    }

    fun deletePrompt(id: String) {
        val current = _prompts.value.filter { it.id != id }
        saveToPrefs(current)
    }

    private fun saveToPrefs(list: List<Prompt>) {
        val jsonArray = JSONArray()
        for (p in list) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("title", p.title)
            obj.put("text", p.text)
            jsonArray.put(obj)
        }
        prefs.edit().putString("prompts_json", jsonArray.toString()).apply()
        _prompts.value = list
    }
}
