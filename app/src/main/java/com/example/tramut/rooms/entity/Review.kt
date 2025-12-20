package com.example.tramut.rooms.entity

import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val id: String = "",
    val bookingId: String = "",
    val bookingDate: String = "",
    val venue: String = "",
    val venueType: String = "",
    val loginId: String = "",
    val issueCategory: String = "",
    val comment: String = "",
    val status: String = "Unsolved",
    val department: String = "",
    val userId: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
