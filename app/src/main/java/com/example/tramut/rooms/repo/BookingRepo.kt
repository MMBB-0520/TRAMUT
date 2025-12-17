package com.example.tramut.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking

class BookingRepo {
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