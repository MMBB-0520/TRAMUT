package com.example.tramut.userInterface

import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.viewModel.TimetableViewModel // Make sure this import is correct
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    facilityId: String,
    onNavigateBack: () -> Unit, // Added navigation back support
    viewModel: TimetableViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // --- STATE FOR DIALOG ---
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }

    // Load data when entering screen
    LaunchedEffect(facilityId) {
        viewModel.loadFacility(facilityId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(uiState.currentFacility?.name ?: "Loading...", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFFEEEEF2)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // 1. DATE PICKER ROW (Uses the component you provided below)
            AdminDatePickerRow(
                currentDate = uiState.selectedDate,
                onDateSelected = { newDate ->
                    viewModel.updateDate(newDate) // Update ViewModel when date changes
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. THE GRID
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4), // 4 columns
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Loop from 08:00 to 22:00
                    items((8..22).toList()) { hour ->

                        // ASK VIEWMODEL FOR STATUS
                        val status = viewModel.getSlotStatus(hour)

                        // SET COLOR BASED ON STATUS
                        val (bgColor, textColor) = when (status) {
                            "Available" -> Color(0xFF4CAF50) to Color.White // Green
                            "Booked" -> Color(0xFF2196F3) to Color.White    // Blue
                            "Maintenance" -> Color(0xFFF44336) to Color.White // Red
                            "Closed" -> Color.LightGray to Color.DarkGray   // Grey
                            else -> Color.Gray to Color.White
                        }

                        Box(
                            modifier = Modifier
                                .height(60.dp)
                                .background(bgColor, RoundedCornerShape(8.dp))
                                // Only clickable if "Available"
                                .clickable(enabled = status == "Available") {
                                    selectedHour = hour
                                    showConfirmDialog = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format("%02d:00", hour),
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = status,
                                    fontSize = 10.sp,
                                    color = textColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 3. CONFIRMATION DIALOG
    if (showConfirmDialog && selectedHour != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Booking") },
            text = {
                Column {
                    Text("Date: ${uiState.selectedDate}")
                    Text("Time: ${String.format("%02d:00", selectedHour)} - ${String.format("%02d:00", selectedHour!! + 1)}")
                    Text("Facility: ${uiState.currentFacility?.name}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // CALL THE BOOKING FUNCTION
                        viewModel.bookSlot(
                            hour = selectedHour!!,
                            onSuccess = {
                                showConfirmDialog = false
                                Toast.makeText(context, "Booking Successful!", Toast.LENGTH_SHORT).show()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color.White
        )
    }
}

// --- HELPER COMPONENT: DATE PICKER ---
@Composable
fun AdminDatePickerRow(
    currentDate: String,
    onDateSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Parse current selected date to set the picker start
    val parts = currentDate.split("-")
    if (parts.size == 3) {
        // Note: Month is 0-indexed in Calendar, but 1-indexed in String
        calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            // Format: YYYY-MM-DD
            val formattedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
            onDateSelected(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
            .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.DateRange, contentDescription = null, tint = Color.Black)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Viewing Date: $currentDate",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}