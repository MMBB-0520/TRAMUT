package com.example.tramut.userInterface.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tramut.ui.theme.Background

@Composable
fun SettingsScreen(
    onChangePasswordClick: () -> Unit,
    onThemeClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {

        Spacer(modifier = Modifier.height(10.dp))

        // Account Section
        SettingsSection {
            SettingsItem(
                title = "Change Password",
                icon = Icons.Default.Lock,
                onClick = onChangePasswordClick
            )
        }

        // Appearance / Theme Section
        SettingsSection {
            SettingsItem(
                title = "Theme",
                icon = Icons.Default.Build,
                onClick = onThemeClick
            )
        }

        // Privacy Section
        SettingsSection {
            SettingsItem(
                title = "Privacy Policy",
                icon = Icons.Default.AccountBox,
                onClick = onPrivacyClick
            )
        }

        // About Section
        SettingsSection {
            SettingsItem(
                title = "About App",
                icon = Icons.Default.Info,
                onClick = onAboutClick
            )
        }
    }
}
@Composable
fun SettingsSection(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {

        content()
        Divider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
fun SettingsItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF512DA8)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))


    }
}

@Preview (showBackground = true, showSystemUi = true)
@Composable
fun SettingsSectionPreview() {
    SettingsScreen(
        onChangePasswordClick = {},
        onThemeClick = {},
        onPrivacyClick = {},
        onAboutClick = {}
    )
}
