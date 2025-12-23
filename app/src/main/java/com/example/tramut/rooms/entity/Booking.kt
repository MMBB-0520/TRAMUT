package com.example.tramut.rooms.entity

import com.google.firebase.firestore.PropertyName


data class Booking(
    val bookingId: String = "",
    val userId: String = "",
    val timeslotId: String = "",
    val facility: String = "",
    val venue: String = "",
    val level: String = "",
    val building: String = "",
    val duration: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val checkIn: String = "",
    val checkOut: String = "",
    val bookingNo: String = "",
    val members: List<Member> = emptyList(),
    val finalVenue: String = "",
    val pax: Int=1,

    @get:PropertyName("status") @set:PropertyName("status") var status: String = "Booked",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",

    @get:PropertyName("hoursList") @set:PropertyName("hoursList") var hoursList: List<Int> = emptyList()
)

data class Member(
    val id: String = "",
    val name: String = ""
)