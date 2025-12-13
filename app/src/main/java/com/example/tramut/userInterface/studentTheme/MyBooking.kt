package com.example.myfacilitybookingsystem.userInterface.studentTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.graphics.Color
import android.util.Log
import androidx.navigation.NavHostController

@Composable
fun MyBookingScreen(userId: String,navController: NavHostController) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch bookings from Firestore
    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance()
            .collection("sportBookings")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                bookings = querySnapshot.documents.map { doc ->
                    Booking(
                        facility = doc.getString("facility") ?: "",
                        bookingNo = doc.getString("bookingId") ?: doc.id,
                        date = doc.getString("date") ?: "",
                        duration = doc.getString("duration") ?: "",
                        venue = doc.getString("venue") ?: "",
                        level = doc.getString("level") ?: "",
                        building = doc.getString("building") ?: "",
                        checkIn = doc.getString("checkIn") ?: "",
                        checkOut = doc.getString("checkOut") ?: "",
                        status = doc.getString("status") ?: "Booked"
                    )
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                Log.e("MyBookingScreen", "Failed to fetch bookings", e)
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Not have booking yet.")
            }
        } else {
            bookings.forEach { booking ->
                BookingItem(booking) {
                    navController.navigate("BookingInfo/${booking.bookingNo}")
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun BookingItem(booking: Booking, onBookInfo: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookInfo()},
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Facility: ${booking.facility}")
            Text("Venue: ${booking.venue}")
            Text("Date: ${booking.date}")
            Text("Time: ${booking.duration}")
            Text("Status: ${booking.status}")
        }
    }
}
