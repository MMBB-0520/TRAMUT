package com.example.tramut.rooms.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Booking")
data class Booking(

    @PrimaryKey
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
    val members: List<Pair<String,String>> = emptyList()
)
