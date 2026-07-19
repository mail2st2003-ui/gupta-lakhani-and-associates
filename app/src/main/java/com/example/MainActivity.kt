package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.ui.OnSiteApp
import com.example.ui.OnSiteViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Crash Reporter Setup
    val prefs = getSharedPreferences("crash_prefs", android.content.Context.MODE_PRIVATE)
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
        val stackTrace = android.util.Log.getStackTraceString(exception)
        prefs.edit().putString("last_crash", stackTrace).commit()
        defaultHandler?.uncaughtException(thread, exception)
    }

    enableEdgeToEdge()
    setContent {
      val viewModel: OnSiteViewModel = viewModel()
      val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
      val colorTheme by viewModel.colorTheme.collectAsStateWithLifecycle()

      var crashLog by androidx.compose.runtime.remember { 
          androidx.compose.runtime.mutableStateOf(prefs.getString("last_crash", null)) 
      }

      MyApplicationTheme(
        darkTheme = isDarkMode,
        themeName = colorTheme
      ) {
        if (crashLog != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { 
                    crashLog = null
                    prefs.edit().remove("last_crash").apply()
                },
                title = { androidx.compose.material3.Text("App Crashed Previously") },
                text = { 
                    androidx.compose.foundation.layout.Column(
                        modifier = androidx.compose.ui.Modifier.verticalScroll(rememberScrollState())
                    ) {
                        androidx.compose.material3.Text(
                            text = crashLog ?: "",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { 
                        crashLog = null
                        prefs.edit().remove("last_crash").apply()
                    }) {
                        androidx.compose.material3.Text("Dismiss")
                    }
                }
            )
        }
        OnSiteApp(viewModel = viewModel)
      }
    }
  }
}

