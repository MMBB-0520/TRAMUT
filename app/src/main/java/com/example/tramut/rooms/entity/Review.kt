package com.example.tramut.rooms.entity

import com.google.firebase.firestore.DocumentId

data class Review (
    @DocumentId val id: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val userName: String = "",

    val issueCategory: String = "",
    val comment: String = "",
    val status: String = "Unsolved",

    val venue: String = "",
    val venueType: String = "",
    val department:String ="",
    val bookingDate: String = "",
    val timestamp: Long = System.currentTimeMillis()

)