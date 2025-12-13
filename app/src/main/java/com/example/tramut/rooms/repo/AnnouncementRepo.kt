package com.example.myfacilitybookingsystem.rooms.repo

import kotlin.jvm.java
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale

class AnnouncementRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("announcement")
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // 1. GET LIST (Real-time updates)
    fun getAnnouncementsFlow(department: String): Flow<List<Announcement>> = callbackFlow {
        val listener = collection
            .whereEqualTo("department", department)
            .orderBy("created_date_str", Query.Direction.DESCENDING) // Sort by date string or add a real timestamp field if needed
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.map { doc ->
                        doc.toObject(Announcement::class.java)!!.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { listener.remove() }
    }

    // 2. GET SINGLE (For Edit)
    suspend fun getAnnouncementById(id: String): Announcement? {
        return try {
            val doc = collection.document(id).get().await()
            if (doc.exists()) {
                doc.toObject(Announcement::class.java)?.copy(id = doc.id)
            } else null
        } catch (e: Exception) { null }
    }

    // 3. ADD
    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            collection.add(announcement).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. UPDATE
    suspend fun updateAnnouncement(
        docId: String,
        title: String,
        content: String,
        venue: String,
        startDate: String,
        endDate: String
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "title" to title,
                "content" to content,
                "venue" to venue,
                "created_date_str" to startDate,
                "expiry_date_str" to endDate
            )
            collection.document(docId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 5. DELETE
    suspend fun deleteAnnouncement(docId: String): Result<Unit> {
        return try {
            collection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}