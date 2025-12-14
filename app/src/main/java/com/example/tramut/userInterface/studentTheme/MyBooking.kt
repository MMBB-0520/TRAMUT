package com.example.myfacilitybookingsystem.userInterface.studentTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import androidx.compose.ui.text.font.FontWeight

@Composable
fun MyBookingScreen(
    navController: NavController,
    userId: String
) {
    var bookingList by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        FirebaseFirestore.getInstance()
            .collection("bookings")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                isLoading = false

                if (error != null) {
                    Log.e("MyBookingScreen", "Error fetching bookings", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    bookingList = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(
                            bookingNo = doc.getString("bookingId") ?: doc.id
                        )
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (bookingList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookings found")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bookingList) { booking ->
                    MyBookingItem(
                        booking = booking,
                        onClick = {
                            navController.navigate("BookingInfo/${booking.bookingNo}")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MyBookingItem(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = booking.venue ?: "Unknown Venue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Date: ${booking.date ?: "N/A"}")
            Text("Time: ${booking.duration ?: "N/A"}")
            Text(
                text = "Status: ${booking.status ?: "Booked"}",
                color = when (booking.status) {
                    "Booked" -> Color.Green
                    "Cancelled" -> Color.Red
                    "Pending" -> Color(0xFFFFA500)
                    else -> Color.Gray
                }
            )
        }
    }
}