package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.OnSiteApp
import com.example.ui.OnSiteViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: OnSiteViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
      val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()

      MyApplicationTheme(
        darkTheme = isDarkMode,
        themeName = colorTheme
      ) {
        OnSiteApp(viewModel = viewModel)
      }
    }
  }
}

