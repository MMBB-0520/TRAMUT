package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.viewModel.TimetableViewModel

@Composable
fun TimetableGrid(
    facilities: List<Facility>,
    viewModel: TimetableViewModel
) {
    // Shared Design Dimensions
    val venueColWidth = 130.dp
    val timeColWidth = 60.dp
    val rowHeight = 35.dp
    val borderColor = Color(0xFFE0E0E0)

    Column {
        val verticalScroll = rememberScrollState()
        val horizontalScroll = rememberScrollState()

        // --- HEADER ROW ---
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            // Corner Box
            Box(
                modifier = Modifier
                    .width(venueColWidth)
                    .height(rowHeight)
                    .background(Color.White)
                    .border(1.dp, borderColor),
                contentAlignment = Alignment.Center
            ) {
                Text("Venue/Time", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Time Headers
            (8..22).forEach { hour ->
                Box(
                    modifier = Modifier
                        .width(timeColWidth)
                        .height(rowHeight)
                        .background(Color.White)
                        .border(1.dp, borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:00", hour),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // --- DATA ROWS ---
        Column(modifier = Modifier.verticalScroll(verticalScroll)) {
            facilities.forEach { facility ->
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {

                    // Venue Name
                    Box(
                        modifier = Modifier
                            .width(venueColWidth)
                            .height(rowHeight)
                            .background(Color.White)
                            .border(1.dp, borderColor),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = facility.name,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Slots (The "Live" Part)
                    (8..22).forEach { hour ->
                        // This calls the ViewModel to check if Admin booked it
                        val status = viewModel.getSlotStatus(facility, hour)

                        val cellColor = when (status) {
                            "Available" -> Color(0xFF4CAF50)
                            "Booked" -> Color(0xFF2196F3)
                            "Maintenance" -> Color(0xFFF44336)
                            else -> Color(0xFFE0E0E0)
                        }

                        Box(
                            modifier = Modifier
                                .width(timeColWidth)
                                .height(rowHeight)
                                .background(cellColor)
                                .border(0.5.dp, Color.White)
                        )
                    }
                }
            }
        }
    }
}


//@Composable
//fun StudentBookingPage(
//    departmentName: String, // "Sport", etc.
//    viewModel: TimetableViewModel = viewModel() // Uses same VM as Admin
//) {
//    val uiState by viewModel.uiState.collectAsState()
//
//    // 1. Load Data Automatically when this screen opens
//    LaunchedEffect(Unit) {
//        val today = java.time.LocalDate.now().toString()
//        viewModel.updateDate(today) // Set to today
//        viewModel.loadFacilities(departmentName) // Load live data
//    }
//
//    Scaffold { padding ->
//        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
//
//            Text("Book a Slot", fontSize = 24.sp, fontWeight = FontWeight.Bold)
//
//            // ... Their Booking Form Inputs (Name, ID, etc.) ...
//            // ... Their Booking Form Inputs ...
//
//            Spacer(Modifier.height(20.dp))
//
//            Text("Current Availability:", fontWeight = FontWeight.Bold)
//            Spacer(Modifier.height(10.dp))
//
//            // 2. INSERT THE SHARED GRID HERE
//            // It will automatically show the red/blue boxes if Admin updated them
//            if (uiState.isLoading) {
//                CircularProgressIndicator()
//            } else {
//                TimetableGrid(
//                    facilities = uiState.facilitiesList,
//                    viewModel = viewModel
//                )
//            }
//        }
//    }
//}