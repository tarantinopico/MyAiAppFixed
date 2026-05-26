package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.screens.MainAppScaffold
import com.example.ui.theme.AIModelAggregatorTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val appContainer = (application as AIModelAggregatorApplication).container
    setContent {
      AIModelAggregatorTheme {
        MainAppScaffold(appContainer)
      }
    }
  }
}
