package com.example.tramut.viewModel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.ReviewRepo
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: ReviewRepo = ReviewRepo()) : ViewModel() {
    private val _view = MutableStateFlow<Booking?>(null)
    val view: StateFlow<Booking?> = _view
    var isSaving by mutableStateOf(false)
        private set

    var showSuccessDialog by mutableStateOf(false)
        private set

    fun submitReview(
        booking: Booking?,
        category: String,
        otherDetail: String,
        description: String,
        userId: String,
        userName: String
    ) {
        viewModelScope.launch {
            isSaving = true

            // 1. Determine the final category string
            val finalCategory = if (category == "Other") otherDetail else category

            // 2. Map data to match your Review Data Class exactly
            val reviewData = hashMapOf(
                "bookingId" to (booking?.bookingId ?: ""),
                "userId" to userId,
                "userName" to userName,
                "issueCategory" to finalCategory,
                "comment" to description,
                "status" to "Unsolved",
                "venue" to (booking?.venue ?: ""),
                "venueType" to (booking?.facility ?: ""),
                "department" to (booking?.facility ?: ""),
                "bookingDate" to (booking?.date ?: ""),
                "timestamp" to System.currentTimeMillis()
            )

            // 3. Submit to repository
            val success = repository.submitReview(reviewData)
            if (success) showSuccessDialog = true
            isSaving = false
        }
    }

    fun updateStatus(reviewId: String, newStatus: String, onSuccess: () -> Unit) {
        Firebase.firestore.collection("reviews")
            .document(reviewId)
            .update("status", newStatus)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun dismissSuccess() {
        showSuccessDialog = false
    }

    fun getBookingFromId(bookingId: String) {
        Firebase.firestore.collection("bookings")
            .document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val booking = document.toObject(Booking::class.java)
                    _view.value = booking
                }
            }
    }
}