package com.example.myfacilitybookingsystem.rooms.entity

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Facility(
    @get:Exclude var id: String = "",

    @get:PropertyName("facility_type") @set:PropertyName("facility_type")
    var name: String = "",

    @get:PropertyName("facility_type") @set:PropertyName("facility_type")
    var department: String = "",

    @get:PropertyName("facility_status") @set:PropertyName("facility_status")
    var status: String = "Available",

    @get:PropertyName("capacity") @set:PropertyName("capacity")
    var capacity: Int = 0,

    @get:PropertyName("start_time") @set:PropertyName("start_time")
    var startTime: String = "08:00",

    @get:PropertyName("end_time") @set:PropertyName("end_time")
    var endTime: String = "22:00",

    @get:PropertyName("special_closures") @set:PropertyName("special_closures")
    var specialClosures: Map<String, List<Int>> = emptyMap(),

    @get:PropertyName("facility_name") @set:PropertyName("facility_name")
    var roomCode: String = ""

)
