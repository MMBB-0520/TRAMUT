package com.example.tramut.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// In your BookingRepo.kt file
class BookingRepo {
    private val firestore = FirebaseFirestore.getInstance()

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

    suspend fun getFacilitiesByCategoryAndCapacity(category: String, minCapacity: Int): List<Facility> {
        return try {
            firestore.collection("facilities")
                .whereEqualTo("category", category)
                .whereGreaterThanOrEqualTo("capacity", minCapacity) // Filter by size
                .get()
                .await()
                .toObjects(Facility::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }



    suspend fun saveBooking(booking: Booking): Boolean {
        return try {
            firestore.collection("bookings")
                .document(booking.bookingId)
                .set(booking)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}