package com.example.myfacilitybookingsystem.userInterface.adminTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// --- COLORS (If you don't have them in ui.theme) ---
// You can remove these lines if you already import them from your theme file
val BorderGray = Color(0xFFE0E0E0)
val CheckGreen = Color(0xFF4CAF50)

// ----------------------------------------------------------------
// 1. LABELED INPUT
// Wraps any content (TextField, Dropdown, etc.) with a bold label above it.
// ----------------------------------------------------------------
@Composable
fun LabeledInput(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

// ----------------------------------------------------------------
// 2. TRANSPARENT TEXT FIELD
// A clean text input with just a bottom underline (looks like a form).
// ----------------------------------------------------------------
@Composable
fun TransparentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String = ""
) {
    Column {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground),
            keyboardOptions = keyboardOptions,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            decorationBox = { innerTextField ->
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(placeholder, color = Color.Gray, fontSize = 16.sp)
                }
                innerTextField()
            }
        )
        // Using HorizontalDivider for Material3 (or Divider for older versions)
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
    }
}

// ----------------------------------------------------------------
// 3. STATIC INPUT TEXT
// Used for Read-Only fields (like Department) or Dropdown triggers.
// ----------------------------------------------------------------
@Composable
fun StaticInputText(
    text: String,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null
) {
    val modifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                color = if (text == "Select Venue" || text.isEmpty()) Color.Gray else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            if (icon != null) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.onBackground)
            }
        }
        HorizontalDivider(thickness = 1.dp, color = Color.LightGray)
    }
}

// ----------------------------------------------------------------
// 4. SUCCESS POPUP
// A simple dialog shown after adding/editing data.
// ----------------------------------------------------------------
@Composable
fun SuccessPopup(message: String = "Success", onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BorderGray),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(CheckGreen.copy(alpha = 0.1f), CircleShape)
                        .border(2.dp, CheckGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = CheckGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CheckGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}