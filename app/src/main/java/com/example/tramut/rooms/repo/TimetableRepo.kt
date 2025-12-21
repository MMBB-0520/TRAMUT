package com.example.tramut.rooms.repo

import com.example.tramut.rooms.entity.Booking
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class TimetableRepository {


    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")
    private val facilitiesCollection = db.collection("facilities")

    suspend fun getFacilitiesByDepartment(department: String): List<Facility> {
        return try {
            // Fetch ALL facilities that belong to "Library" (or "Sports", etc.)
            val snapshot = facilitiesCollection
                .whereEqualTo("department", department) // Ensure your Firestore field is "department"
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Facility::class.java)?.copy(id = doc.id)

            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getBookingsForDate(date: String): List<Booking> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("date", date)
                //.whereEqualTo("status", "Confirmed") // Optional: uncomment if needed
                .get().await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(bookingId = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getFacilities(department: String): List<Facility> {
        return try {
            val snapshot = db.collection("facilities")
                .whereEqualTo("facility_type", department)
                .get().await()

            snapshot.documents.mapNotNull { it.toObject(Facility::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Inside your Repository
    suspend fun saveBooking(booking: Booking): Boolean {
        return try {
            FirebaseFirestore.getInstance()
                .collection("bookings")
                .document(booking.bookingId)
                .set(booking)
                .await() // This "waits" for Firebase to finish and returns void
            true // If it reaches here, it succeeded
        } catch (e: Exception) {
            false // If there is a network error, it returns false
        }
    }
}