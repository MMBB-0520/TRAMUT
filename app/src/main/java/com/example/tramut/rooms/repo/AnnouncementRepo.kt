package com.example.myfacilitybookingsystem.rooms.repo

import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AnnouncementRepository {

    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("announcements")

    fun getAnnouncementsFlow(department: String): Flow<List<Announcement>> = callbackFlow {
        // You can add .whereEqualTo("department", department) if you want to filter
        val query = collection.orderBy("created_date_str") // Sort by date

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    // CRITICAL FIX: Copy the Firestore ID into the object
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getAdminAnnouncementsFlow(department: String): Flow<List<Announcement>> = callbackFlow {

        // We KNOW the department is mandatory here, so we apply the filter directly.
        val query = collection
            .whereEqualTo("department", department) // **CRUCIAL: Filters by the admin's department**
            .orderBy("created_date_str")            // Orders the results
            .limit(50)                              // Limits the results

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Log the error
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Announcement::class.java)?.copy(id = doc.id)
                }
                trySend(list)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun getAnnouncementById(id: String): Announcement? {
        return try {
            val snapshot = collection.document(id).get().await()
            // CRITICAL FIX: Copy the ID here too
            snapshot.toObject(Announcement::class.java)?.copy(id = snapshot.id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun addAnnouncement(announcement: Announcement): Result<Unit> {
        return try {
            // We use .add() which generates a random ID
            collection.add(announcement).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAnnouncement(
        docId: String,
        title: String,
        content: String,
        venue: String,
        start: String,
        end: String
    ): Result<Unit> {
        return try {
            val updates = mapOf(
                "title" to title,
                "content" to content,
                "venue" to venue,
                "created_date_str" to start,
                "expiry_date_str" to end
            )
            collection.document(docId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAnnouncement(docId: String): Result<Unit> {
        return try {
            collection.document(docId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}