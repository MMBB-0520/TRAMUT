package com.example.tramut.rooms.entity

data class Booking(
    val facility: String,
    val bookingNo: String,
    val date: String,
    val duration: String,
    val venue: String,
    val level: String,
    val building: String,
    val checkIn: String,
    val checkOut: String,
    val status: String
)