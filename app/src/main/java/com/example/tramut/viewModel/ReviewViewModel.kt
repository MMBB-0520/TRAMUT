package com.example.tramut.viewModel

import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.ReviewRepo
import kotlinx.coroutines.launch

class ReviewViewModel(private val repository: ReviewRepo = ReviewRepo()) : ViewModel() {

    var isSaving by mutableStateOf(false)
        private set

    var showSuccessDialog by mutableStateOf(false)
        private set

    // Called by the User Screen
    fun submitReview(
        booking: Booking,
        category: String,
        otherDetail: String,
        description: String,
        context: Context
    ) {
        viewModelScope.launch {
            isSaving = true
            val finalCategory = if (category == "Other") otherDetail else category

            val reviewData = hashMapOf(
                "bookingId" to booking.bookingId,
                "bookingNo" to booking.bookingNo,
                "userId" to booking.userId,
                "userName" to booking.userName,
                "venueType" to booking.facility,
                "department" to booking.venueType,
                "venue" to booking.venue,
                "bookingDate" to booking.date,
                "issueCategory" to finalCategory,
                "comment" to description,
                "status" to "Unsolved", // Default
                "timestamp" to System.currentTimeMillis()
            )

            val success = repository.submitReview(reviewData)
            if (success) showSuccessDialog = true
            isSaving = false
        }
    }

    // Called by the Admin Screen to solve the issue
    fun updateStatus(reviewId: String, newStatus: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isSaving = true
            val success = repository.updateReviewStatus(reviewId, newStatus)
            if (success) onSuccess()
            isSaving = false
        }
    }

    fun dismissSuccess() {
        showSuccessDialog = false
    }
}