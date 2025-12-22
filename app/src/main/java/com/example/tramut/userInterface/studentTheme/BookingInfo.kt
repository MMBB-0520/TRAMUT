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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.tramut.AppScreen
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.ui.theme.StaffRed
import com.example.tramut.ui.theme.StudentBlue
import com.example.tramut.viewModel.MyBookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingInfoScreen(
    booking: Booking,
    navController: NavHostController,
    containerColor: Color,
    viewModel: MyBookingViewModel = viewModel()
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    val level by remember(booking.venue) {
        derivedStateOf { viewModel.getLevelForVenue(booking.venue) }
    }
    val building by remember(booking.venue) {
        derivedStateOf { viewModel.getBuildingForVenue(booking.venue) }
    }

    val facilityCode = remember(booking.bookingId) {
        val category = booking.facility

        when {
            category.contains("Badminton", ignoreCase = true) ||
                    category.contains("Snooker", ignoreCase = true) ||
                    category.contains("Squash", ignoreCase = true) ||
                    category.contains("Gym", ignoreCase = true) ||
                    category.contains("Pool", ignoreCase = true) ||
                    category.contains("Tennis", ignoreCase = true) ||
                    category.contains("Futsal", ignoreCase = true) ||
                    category.contains("Guest", ignoreCase = true) ||
                    category.contains("ball", ignoreCase = true) -> SCode()

                    category.contains("CC", ignoreCase = true) -> CCode()

            else -> LCode()
        }
    }

    // 监听UI状态变化 (保持不变)
    LaunchedEffect(uiState) {
        when (uiState) {
            MyBookingViewModel.BookingUIState.Success -> {
                showCancelDialog = false
                showSuccessDialog = true
            }
            is MyBookingViewModel.BookingUIState.Error -> {
                showCancelDialog = false
                errorMessage = (uiState as MyBookingViewModel.BookingUIState.Error).message
            }
            else -> {}
        }
        // IMPORTANT: Reset UI state after handling success/error to prevent re-triggering
        if (uiState is MyBookingViewModel.BookingUIState.Success || uiState is MyBookingViewModel.BookingUIState.Error) {
            viewModel.resetUIState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                title = {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 48.dp), contentAlignment = Alignment.Center) {
                        Text("Booking Information", fontWeight = FontWeight.SemiBold, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF5F5F5))
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "Booking Details",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

            Divider(color = Color.LightGray)

            BookingDetailRow("Facility", booking.facility)
            BookingDetailRow("Booking No.", facilityCode)
            BookingDetailRow("Date", booking.date)
            BookingDetailRow("Duration", booking.duration)
            BookingDetailRow("Venue", booking.venue)
            BookingDetailRow("Court/Room No.", booking.finalVenue)
            BookingDetailRow("Level", level)
            BookingDetailRow("Building", building)
            BookingDetailRow("Check-in", booking.checkIn)
            BookingDetailRow("Check-out", booking.checkOut)
            BookingDetailRow("Status", booking.status)
        }

            Spacer(modifier = Modifier.height(24.dp))

            val canCancel = booking.status.equals("Booked", ignoreCase = true) ||
                    booking.status.equals("Valid", ignoreCase = true) ||
                    booking.status.equals("confirmed", ignoreCase = true)

            if (canCancel) {
                if (uiState is MyBookingViewModel.BookingUIState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    Button(
                        onClick = { showCancelDialog = true },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("CANCEL BOOKING", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "Unknown error") },
            confirmButton = { TextButton(onClick = { errorMessage = null }) { Text("OK") } }
        )
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Booking") },
            text = { Text("Are you sure you want to cancel this booking?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelBookingWithScope(booking.bookingId)
                    }
                ) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }

    if (showSuccessDialog) {
        SuccessCancelDialog(
            onOk = { showSuccessDialog = false; navController.popBackStack() },
            onDismiss = { showSuccessDialog = false; navController.popBackStack() }
        )
    }
}

@Composable
fun BookingDetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.5f)
            )
        }
        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)
    }
}

fun generate9UniqueDigits(): String = (1..9).map { (0..9).random() }.joinToString("")
fun LCode(): String = "L${generate9UniqueDigits()}"
fun CCode(): String = "C${generate9UniqueDigits()}"
fun SCode(): String = "S${generate9UniqueDigits()}"