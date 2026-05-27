package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.AIModelAggregatorTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appContainer = (application as AIModelAggregatorApplication).container
    setContent {
      val themeMode by appContainer.themePreferences.themeMode.collectAsStateWithLifecycle()
      val isDarkTheme = when (themeMode) {
          1 -> false // Light
          2 -> true  // Dark
          else -> androidx.compose.foundation.isSystemInDarkTheme() // System
      }
      AIModelAggregatorTheme(darkTheme = isDarkTheme) {
        MainAppScaffold(appContainer)
      }
    }
  }
}
