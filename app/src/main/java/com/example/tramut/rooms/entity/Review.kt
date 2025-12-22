package com.example.tramut.rooms.entity

import com.google.firebase.firestore.DocumentId

data class Review(
    @DocumentId
    val id: String = "",
    val bookingId: String = "", //（bok-date，venue，venuetype）
    val bookingDate: String = "",
    val venue: String = "",
    val finalVenue: String = "",
    val loginId: String = "",
    val issueCategory: String = "",
    val comment: String = "",
    val status: String = "Unresolved",
    val department: String = "" //facilityType
)
