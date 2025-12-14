package com.example.tramut.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Booking
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java

class TimetableRepository {
    private val db = FirebaseFirestore.getInstance()

    // Get Facilities filtered by Department (using your "facility_type" field)
    suspend fun getFacilities(department: String): List<Facility> {
        return try {
            val snapshot = db.collection("facilities")
                .whereEqualTo("facility_type", department)
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Facility::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Get Active Bookings for a specific Date
    suspend fun getBookingsForDate(date: String): List<Booking> {
        return try {
            val snapshot = db.collection("bookings")
                .whereEqualTo("date", date)
                .whereEqualTo("status", "Confirmed")
                .get().await()
            snapshot.documents.mapNotNull { it.toObject(Booking::class.java)?.copy(id = it.id) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}