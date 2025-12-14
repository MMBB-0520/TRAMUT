package com.example.myfacilitybookingsystem.rooms.entity

import com.google.firebase.firestore.PropertyName

data class Facility(
    val id: String = "",

    @get:PropertyName("facility_name") @set:PropertyName("facility_name")
    var name: String = "",

    @get:PropertyName("facility_type") @set:PropertyName("facility_type")
    var department: String = "",

    @get:PropertyName("capacity") @set:PropertyName("capacity")
    var capacity: Int = 0,

    @get:PropertyName("facility_status") @set:PropertyName("facility_status")
    var status: String = "Available",

    @get:PropertyName("start_time") @set:PropertyName("start_time")
    var startTime: String = "08:00",

    @get:PropertyName("end_time") @set:PropertyName("end_time")
    var endTime: String = "22:00",

    @get:PropertyName("special_closures") @set:PropertyName("special_closures")
    var specialClosures: Map<String, List<Int>> = emptyMap()
)

data class Booking(
    val id: String = "",
    val facilityId: String = "",
    val userId: String = "",
    val date: String = "",
    val timeSlot: Int = 0,
    val status: String = "Confirmed"
)