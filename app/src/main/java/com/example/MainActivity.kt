package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StreamingViewModel

class MainActivity : ComponentActivity() {
  private val streamingViewModel: StreamingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val currentScreen by streamingViewModel.currentScreen.collectAsState()
        
        when (currentScreen) {
          "splash" -> LaunchScreen(viewModel = streamingViewModel)
          "auth" -> AuthScreen(viewModel = streamingViewModel)
          "home" -> MainScreen(viewModel = streamingViewModel)
          "player" -> PlayerScreen(viewModel = streamingViewModel)
          else -> LaunchScreen(viewModel = streamingViewModel)
        }
      }
    }
  }
}

