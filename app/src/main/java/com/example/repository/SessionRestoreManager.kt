package com.example.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionRestoreManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)
    
    private val _lastActiveConversationId = MutableStateFlow<Long?>(
        if (prefs.contains("last_conv_id")) prefs.getLong("last_conv_id", -1L).takeIf { it != -1L } else null
    )
    val lastActiveConversationId: StateFlow<Long?> = _lastActiveConversationId.asStateFlow()

    fun setLastActiveConversation(id: Long?) {
        _lastActiveConversationId.value = id
        val editor = prefs.edit()
        if (id != null) {
            editor.putLong("last_conv_id", id)
        } else {
            editor.remove("last_conv_id")
        }
        editor.apply()
    }
}
