package com.example.tramut.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class BookingRepo {

    val db = FirebaseFirestore.getInstance()

    private fun saveNewBooking(
        facilityId: String,
        venueName: String,
        date: String,
        startTime: String,
        endTime: String,
        hoursList: List<Int>,
        userId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val bookingData = hashMapOf(
            "facilityId" to facilityId,
            "venueName" to venueName,
            "date" to date,
            "startTime" to startTime,
            "endTime" to endTime,
            "hoursList" to hoursList,
            "userId" to userId,
            "status" to "Confirmed",
            "bookingStatus" to "Confirmed"
        )

        Firebase.firestore.collection("bookings").add(bookingData)
            .addOnSuccessListener { onComplete(true, "Successfully assigned to $venueName") }
    }

    suspend fun getFacilitiesByCategory(category: String): List<Facility> {
        return try {
            db.collection("facilities")
                .whereEqualTo("category", category)
                .get()
                .await()
                .toObjects(Facility::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fetch existing bookings for a specific date
    suspend fun getBookingsByDate(date: String): List<Booking> {
        return try {
            db.collection("bookings")
                .whereEqualTo("date", date)
                .get()
                .await()
                .toObjects(Booking::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Save the finalized booking
    suspend fun saveBooking(booking: Booking): Result<Unit> {
        return try {
            db.collection("bookings").add(booking).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}