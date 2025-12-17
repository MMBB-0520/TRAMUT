package com.example.tramut.userInterface.check

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.ui.theme.TRAMUTTheme

@Composable
fun CheckInConfirmationScreen(
    onOkClicked: () -> Unit,
    onBackClicked: () -> Unit = {}
) {
    Scaffold(
        topBar = { CheckInTopBar(title = "Check-In", onBackClicked = onBackClicked) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(bottom = 100.dp), // Slight offset to visually center above button
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon Box from snippet
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, // Using Vector to ensure compilation
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text styled like snippet
                Text(
                    text = "Checked-in successfully",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Button(
                onClick = onOkClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
                    .height(60.dp) // Adjusted slightly for screen ergonomics
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(2.dp), // Shape from snippet
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Color from snippet
            ) {
                Text(
                    text = "OK",
                    color = Color.White,
                    fontSize = 24.sp // Font size from snippet
                )
            }
        }
    }
}

@Composable
fun CheckOutConfirmationScreen(
    onOkClicked: () -> Unit,
    onBackClicked: () -> Unit = {}
) {
    Scaffold(
        topBar = { CheckInTopBar(title = "Check-Out", onBackClicked = onBackClicked) }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            // Center Content: Icon and Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(bottom = 100.dp), // Slight offset to visually center above button
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Icon Box from snippet
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, // Using Vector to ensure compilation
                        contentDescription = "Success",
                        tint = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text styled like snippet
                Text(
                    text = "Checked-out successfully",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Button(
                onClick = onOkClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
                    .height(60.dp) // Adjusted slightly for screen ergonomics
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(2.dp), // Shape from snippet
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) // Color from snippet
            ) {
                Text(
                    text = "OK",
                    color = Color.White,
                    fontSize = 24.sp // Font size from snippet
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "2. Confirmation Screen")
@Composable
fun PreviewCheckInConfirmationScreen() {
    TRAMUTTheme {
        CheckInConfirmationScreen(
            onOkClicked = { /* Preview action */ },
            onBackClicked = { /* Preview action */ }
        )
    }
}