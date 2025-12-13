package com.example.myfacilitybookingsystem.rooms.entity

import com.google.firebase.firestore.PropertyName

data class Facility(
    val id: String = "",

    @get:PropertyName("facility_name") @set:PropertyName("facility_name")
    var name: String = "",

    @get:PropertyName("facility_type") @set:PropertyName("facility_type")
    var department: String = "", // "Sport", "CITC", or "Library"

    @get:PropertyName("facility_description") @set:PropertyName("facility_description")
    var description: String = "",

    @get:PropertyName("location") @set:PropertyName("location")
    var location: String = "",

    @get:PropertyName("capacity") @set:PropertyName("capacity")
    var capacity: Int = 0,

    @get:PropertyName("facility_status") @set:PropertyName("facility_status")
    var status: String = "Available",

    @get:PropertyName("start_time") @set:PropertyName("start_time")
    var startTime: String = "08:00",

    @get:PropertyName("end_time") @set:PropertyName("end_time")
    var endTime: String = "22:00"
)