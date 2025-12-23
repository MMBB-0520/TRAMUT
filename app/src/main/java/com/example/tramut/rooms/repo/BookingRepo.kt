package com.example.tramut.rooms.repo

import android.util.Log
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

// In your BookingRepo.kt file
class BookingRepo {
    private val firestore = FirebaseFirestore.getInstance()

    // 1. Fetch bookings by date
    suspend fun getBookingsByDate(date: String): List<Booking> {
        return try {
            firestore.collection("bookings")
                .whereEqualTo("date", date)
                .get()
                .await()
                .toObjects(Booking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }



    suspend fun getFacilitiesByCategory(category: String): List<Facility> {
        return try {
            firestore.collection("facilities")
                .whereEqualTo("category", category)
                .get()
                .await()
                .toObjects(Facility::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun formatDateForDisplay(dateStr: String): String {
        return try {
            // The format coming from your DatePicker
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            // The format stored in your Firestore "date" field
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val date = inputFormat.parse(dateStr)
            if (date != null) outputFormat.format(date) else dateStr
        } catch (e: Exception) {
            // If it's already in the correct format or parsing fails, return original
            dateStr
        }
    }

    // 3. The Save function that returns the 'success' boolean
    suspend fun saveBooking(booking: Booking): Boolean {
        return try {
            firestore.collection("bookings")
                .document(booking.bookingId)
                .set(booking)
                .await() // This is CRITICAL for suspend functions
            true
        } catch (e: Exception) {
            Log.e("Repo", "Save failed: ${e.message}")
            false
        }
    }
}