package com.example.tramut.rooms.repo

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReviewRepo {


    private val db = FirebaseFirestore.getInstance()
    private val reviewsCollection = db.collection("reviews")

    suspend fun submitReview(reviewData: Map<String, Any>): Boolean {
        return try {
            reviewsCollection.add(reviewData).await()
            true
        } catch (e: Exception) { false }
    }

    fun updateStatus(reviewId: String, newStatus: String, onSuccess: () -> Unit) {
        db.collection("reviews").document(reviewId)
            .update("status", newStatus)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("ReviewViewModel", "Error updating status", e)
            }
    }

    suspend fun updateReviewStatus(reviewId: String, newStatus: String): Boolean {
        return try {
            // This targets the specific document in Firebase using its ID
            reviewsCollection.document(reviewId)
                .update("status", newStatus)
                .await()
            true
        } catch (e: Exception) { false }
    }
}