package com.example.tramut.userInterface.check

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.ErrorRed

@Composable
fun CheckOutManualEntryScreen(
    bookingId: String,
    isCheckIn: Boolean = false, // Toggle this for Check-Out
    initialId: String = "",
    onSuccess: (String) -> Unit,
    onBackClicked: () -> Unit = {},
    viewModel: EntryViewModel = viewModel()
) {
    var studentId by remember { mutableStateOf(initialId) }
    val uiState by viewModel.uiState.collectAsState()

    val isIdLengthValid = studentId.isNotBlank() && studentId.length >= 4 && studentId.length <= 7
    val isLoading = uiState is EntryUiState.Loading

    // Text & Strings based on Mode
    val screenTitle = if (isCheckIn) "Check-In" else "Check-Out"
    val successMessage = if (isCheckIn) "Check-In Successful" else "Check-Out Successful"

    // Handle Success Navigation
    LaunchedEffect(uiState) {
        if (uiState is EntryUiState.Success) {
            onSuccess(successMessage)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = { CheckInTopBar(title = screenTitle, onBackClicked = onBackClicked) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Student/Staff ID",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            OutlinedTextField(
                value = studentId,
                onValueChange = {
                    if (it.length <= 10) {
                        studentId = it.filter { char -> char.isDigit() }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkHeader,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (isIdLengthValid) {
                        if (isCheckIn) {
                            viewModel.performCheckIn(bookingId, studentId)
                        } else {
                            viewModel.performCheckOut(bookingId, studentId)
                        }
                    }
                },
                enabled = isIdLengthValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkHeader)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Verifying...", color = Color.White)
                } else {
                    Text(text = "Confirmation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Error Dialog
        if (uiState is EntryUiState.Error) {
            val errorMessage = (uiState as EntryUiState.Error).message
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                title = { Text(text = "Action Failed", fontWeight = FontWeight.Bold) },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetState() },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("OK", color = Color.White)
                    }
                },
                containerColor = Background,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}