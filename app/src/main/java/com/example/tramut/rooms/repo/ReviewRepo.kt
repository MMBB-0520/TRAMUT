package com.example.tramut.rooms.repo

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