package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.viewModel.MyBookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingInfoScreen(
    booking: Booking,
    navController: NavHostController,
    viewModel: MyBookingViewModel = viewModel()
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    // 监听UI状态变化
    LaunchedEffect(uiState) {
        when (uiState) {
            is MyBookingViewModel.BookingUIState.Loading -> {
            }
            is MyBookingViewModel.BookingUIState.Success -> {
                showCancelDialog = false
                showSuccessDialog = true
                // 重置状态，避免重复触发
                viewModel.resetUIState()
            }
            is MyBookingViewModel.BookingUIState.Error -> {
                showCancelDialog = false
                val error = (uiState as MyBookingViewModel.BookingUIState.Error)
                errorMessage = error.message
                // 重置状态
                viewModel.resetUIState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .padding(10.dp)
        ) {
            Text(
                "Booking Details",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Divider(color = Color.LightGray)

            BookingDetailRow("Facility", booking.facility)
            BookingDetailRow("Booking No.", booking.bookingNo)
            BookingDetailRow("Date", booking.date)
            BookingDetailRow("Duration", booking.duration)
            BookingDetailRow("Venue / Room No.", booking.venue)
            BookingDetailRow("Level", booking.level)
            BookingDetailRow("Building", booking.building)
            BookingDetailRow("Check-in", booking.checkIn)
            BookingDetailRow("Check-out", booking.checkOut)
            BookingDetailRow("Status", booking.status)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 只在状态是有效时显示取消按钮
        val canCancel = booking.status.equals("Booked", ignoreCase = true) ||
                booking.status.equals("Valid", ignoreCase = true) ||
                booking.status.equals("confirmed", ignoreCase = true)

        if (canCancel) {
            when (uiState) {
                is MyBookingViewModel.BookingUIState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Button(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("CANCEL BOOKING", color = Color.White, fontSize = 20.sp)
                    }
                }
            }
        }

    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Confirmation") },
            text = { Text("Are you sure you want to cancel this booking?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialog = false
                        showSuccessDialog = true
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }

    if (showSuccessDialog) {
        SuccessCancelDialog(
            onOk = {
                showSuccessDialog = false
                navController.popBackStack()
            },
            onDismiss = {
                showSuccessDialog = false
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun BookingDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.DarkGray)
            Text(text = value, fontWeight = FontWeight.SemiBold)
        }
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}