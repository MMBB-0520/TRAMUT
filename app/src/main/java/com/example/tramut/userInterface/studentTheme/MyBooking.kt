package com.example.tramut.userInterface.studentTheme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tramut.AppScreen
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.viewModel.MyBookingViewModel

enum class BookingTab {
    UPCOMING, PENDING, CANCELLED, ALL
}

@Composable
fun MyBookingScreen(
    navController: NavController,
    viewModel: MyBookingViewModel = viewModel(),
    userId: String
) {
    // 1. Initialize data fetching
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            viewModel.startListening(userId)
        }
    }

    val bookingList by viewModel.bookingList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(BookingTab.ALL) }

    var paxInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Guard for empty userId
    if (userId.isBlank()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF2196F3))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // --- TAB SELECTOR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookingTab.values().forEach { tab ->
                BookingTabButton(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }

        // --- FILTERING LOGIC ---
        val filteredBookings = when (selectedTab) {
            BookingTab.UPCOMING -> bookingList.filter {
                it.status.equals("Booked", ignoreCase = true) ||
                        it.status.equals("Valid", ignoreCase = true) ||
                        it.status.equals("confirmed", ignoreCase = true)
            }
            BookingTab.PENDING -> bookingList.filter {
                it.status.equals("Pending", ignoreCase = true) ||
                        it.status.equals("waiting", ignoreCase = true)
            }
            BookingTab.CANCELLED -> bookingList.filter {
                it.status.equals("Cancelled", ignoreCase = true) ||
                        it.status.equals("canceled", ignoreCase = true)
            }
            BookingTab.ALL -> bookingList
        }

        // --- CONTENT AREA ---
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filteredBookings.isEmpty()) {
                val emptyMessage = when (selectedTab) {
                    BookingTab.UPCOMING -> "No upcoming bookings"
                    BookingTab.PENDING -> "No pending bookings"
                    BookingTab.CANCELLED -> "No cancelled bookings"
                    BookingTab.ALL -> "No bookings found"
                }
                Text(emptyMessage, color = Color.Gray, modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings) { booking ->
                        MyBookingItem(
                            booking = booking,
                            onClick = {
                                navController.navigate("${AppScreen.StudentBookingDetails.name}/${booking.bookingId}")
                            },
                            onReportClick = {
                                navController.navigate("${AppScreen.UserReview.name}/${booking.bookingId}")
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MyBookingItem(
    booking: Booking,
    onClick: () -> Unit,
    onReportClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Facility & Status
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(booking.facility.ifBlank { "Facility" }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                StatusBadge(status = booking.status)
            }


            Spacer(modifier = Modifier.height(12.dp))

            // Body: Venue
            Text(booking.venue.ifBlank { "Unknown Venue" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            // Details: Date & Time
            Row {
                Text("Date: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(booking.date, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                Text("Time: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(booking.duration, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                Text("Venue: ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(booking.finalVenue, style = MaterialTheme.typography.bodyMedium)
            }




            Spacer(modifier = Modifier.height(8.dp))

            // Location: Building & Level
            if (booking.building.isNotBlank()) {
                Text("${booking.building} • Level ${booking.level}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Footer: ID & Report Action
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("ID: #${booking.bookingNo}", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)

                // Logic: Only show "Report" for non-cancelled bookings
                val canReport = !booking.status.equals("Cancelled", ignoreCase = true) &&
                        !booking.status.equals("Canceled", ignoreCase = true)

                if (canReport) {
                    TextButton(onClick = onReportClick) {
                        Text("Report Issue", color = Color(0xFF0C1DBC), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BookingTabButton(tab: BookingTab, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color(0xFF2196F3) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() }
            .background(backgroundColor, MaterialTheme.shapes.small)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = tab.name.lowercase().replaceFirstChar { it.uppercase() }, color = textColor, fontSize = 14.sp)
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, txtColor, text) = when (status.lowercase()) {
        "booked", "valid", "confirmed" -> Triple(Color(0xFFE8F5E9), Color(0xFF388E3C), "Confirmed")
        "pending", "waiting" -> Triple(Color(0xFFFFF8E1), Color(0xFFF57C00), "Pending")
        "cancelled", "canceled" -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelled")
        else -> Triple(Color(0xFFF5F5F5), Color.Gray, status)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = txtColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}