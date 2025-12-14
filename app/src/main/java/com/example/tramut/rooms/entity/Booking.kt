package com.example.tramut.rooms.entity

data class Booking(

    val bookingId: String = "",
    val userId: String = "",
    val timeslotId: String = "",
    val checkId: String = "",

    val bookingDate: String = "",
    val bookingStatus: String = "Booked",
    val participantCount: Int = 1,
    val bookingDuration: String = "",


    val facility: String = "",
    val venue: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val venueType: String = "",

    val members: List<Pair<String, String>> = emptyList(),
    val userName: String = "",
    val bookingNo: String,
    val date: String,
    val duration: String,
    val building: String,
    val level: String,
    val checkIn: String,
    val checkOut: String,
    val status: String,
)