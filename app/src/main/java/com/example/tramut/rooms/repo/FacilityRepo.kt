package com.example.myfacilitybookingsystem.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FacilityRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("facilities")

    // 1. GET LIST (Real-time Flow)
    fun getFacilitiesFlow(department: String): Flow<List<Facility>> = callbackFlow {
        val listener = collection
            .whereEqualTo("department", department)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        doc.toObject(Facility::class.java)!!.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    // 2. ADD
    suspend fun addFacility(facility: Facility): Result<Unit> {
        return try {
            collection.add(facility).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. UPDATE
    suspend fun updateFacility(facility: Facility): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "facility_name" to facility.name,
                "capacity" to facility.capacity,
                "description" to facility.description,
                "status" to facility.status
            )
            collection.document(facility.id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. DELETE
    suspend fun deleteFacility(docId: String): Result<Unit> {
        return try {
            collection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}