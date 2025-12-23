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


    suspend fun getFacilitiesByCategory(category: String, pax: Int? = null): List<Facility> {
        return try {
            val snapshot = db.collection("facilities")
                .whereEqualTo("category", category)
                .get()
                .await()

            val allFacilities = snapshot.toObjects(Facility::class.java)

            if (pax == null) {
                allFacilities
            } else {
                allFacilities.filter { facility ->
                    facility.capacity.any { it >= pax }
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    // --- FUNCTION 3: FOR BOOKINGS ---
    // Change name to match what your ViewModel calls (getBookingsByDate)
    suspend fun getBookingsByDate(date: String): List<Booking> {
        return try {
            val snapshot = bookingsCollection
                .whereEqualTo("date", date)
                .get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Booking::class.java)?.copy(bookingId = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- FUNCTION 4: SAVE ---
    suspend fun saveBooking(booking: Booking): Boolean {
        return try {
            bookingsCollection.document(booking.bookingId).set(booking).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
