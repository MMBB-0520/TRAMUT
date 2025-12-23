package com.example.myfacilitybookingsystem.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlin.jvm.java


class FacilityRepository {

    private val db = FirebaseFirestore.getInstance()
    private val facilitiesCollection = db.collection("facilities")
    private val bookingsCollection = db.collection("bookings")
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("facilities")

    fun getFacilityById(facilityId: String, onResult: (Facility?) -> Unit) {
        facilitiesCollection.document(facilityId).get()
            .addOnSuccessListener {
                // We manually set the ID because it's the document key, not a field inside
                val facility = it.toObject(Facility::class.java)?.copy(id = it.id)
                onResult(facility)
            }
            .addOnFailureListener { onResult(null) }
    }

    suspend fun isNameDuplicate(name: String): Boolean {
        return try {
            val result = facilitiesCollection
                .whereEqualTo("name", name.trim())
                .get()
                .await()
            !result.isEmpty // Returns true if it already exists
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getFacilitiesByCategory(category: String): List<Facility> {
        return try {
            collection
                .whereEqualTo("category", category)
                .get()
                .await()
                .toObjects(Facility::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getFacilitiesByLocation(location: String): List<Facility> {
        return try {
            collection
                .whereEqualTo("location", location)
                .get()
                .await()
                .toObjects(Facility::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }


    fun getFacilitiesFlow(department: String): Flow<List<Facility>> = callbackFlow {

        val listener = facilitiesCollection
            .whereEqualTo("department", department)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Facility::class.java)?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addFacility(facility: Facility): Result<Unit> {
        return try {
            facilitiesCollection.add(facility).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFacility(facility: Facility): Result<Unit> {
        return try {

            val updates = mapOf(
                "facility_name" to facility.name,
                "category" to facility.category,
                "department" to facility.department,
                "status" to facility.status,
                "capacity" to facility.capacity,
                "startTime" to facility.startTime,
                "endTime" to facility.endTime,
                "dailyBreakHours" to facility.dailyBreakHours,
                "specialClosures" to facility.specialClosures,
                "duration" to facility.bookedSlots
            )

            facilitiesCollection.document(facility.id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFacility(docId: String): Result<Unit> {
        return try {
            facilitiesCollection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getBookings(facilityName: String, date: String, onResult: (List<Booking>) -> Unit) {
        bookingsCollection
            .whereEqualTo("facility", facilityName)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                val bookings = documents.mapNotNull { it.toObject(Booking::class.java) }
                onResult(bookings)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    suspend fun saveBooking(booking: Booking): Boolean {
        return try {
            bookingsCollection
                .document(booking.bookingId)
                .set(booking)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}