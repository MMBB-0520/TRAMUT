package com.example.tramut.userInterface.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.ui.theme.Background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    viewModel: ThemeViewModel,
    onBackClick: () -> Unit
) {
    val currentMode by viewModel.themeMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Theme", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF512DA8))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(16.dp)
        ) {
            ThemeOption(
                label = "System Default",
                selected = currentMode == AppThemeMode.System,
                onClick = { viewModel.setThemeMode(AppThemeMode.System) }
            )
            ThemeOption(
                label = "Light Mode",
                selected = currentMode == AppThemeMode.Light,
                onClick = { viewModel.setThemeMode(AppThemeMode.Light) }
            )
            ThemeOption(
                label = "Dark Mode",
                selected = currentMode == AppThemeMode.Dark,
                onClick = { viewModel.setThemeMode(AppThemeMode.Dark) }
            )
        }
    }
}

@Composable
fun ThemeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, fontSize = 18.sp)
    }
}