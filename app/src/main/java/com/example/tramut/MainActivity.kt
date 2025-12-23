package com.example.tramut

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.TRAMUTTheme
import com.example.tramut.userInterface.settings.AppThemeMode
import com.example.tramut.userInterface.settings.ThemeViewModel

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 1. Initialize the ThemeViewModel
            val themeViewModel: ThemeViewModel = viewModel()
            val themeMode by themeViewModel.themeMode.collectAsState()

            // 2. Determine if dark mode should be active
            val useDarkTheme = when (themeMode) {
                AppThemeMode.Light -> false
                AppThemeMode.Dark -> true
                AppThemeMode.System -> isSystemInDarkTheme()
            }

            // 3. Pass the boolean to your Theme
            TRAMUTTheme(darkTheme = useDarkTheme) {
                FBSApp(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background),
                    themeViewModel = themeViewModel // Pass VM down to FBSApp
                )
            }
        }
    }
}