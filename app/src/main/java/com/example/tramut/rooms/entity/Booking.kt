package com.example.tramut.rooms.entity


data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val timeslotId: String = "",
    val facility: String = "",
    val venue: String = "",
    val level: String = "",
    val building: String = "",
    val date: String = "",
    val duration: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val bookingNo: String = "",
    val status: String = "Booked",
    val finalVenue: String = "",
    val members: List<Member> = emptyList()
)

data class Member(
    val id: String = "",
    val name: String = ""
)