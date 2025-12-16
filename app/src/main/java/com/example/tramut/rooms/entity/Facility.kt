package com.example.myfacilitybookingsystem.rooms.entity

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Facility(
    @get:Exclude var id: String = "",

    @get:PropertyName("name") @set:PropertyName("name")
    var name: String = "",

    var category: String = "",

    var status:String ="",

    var capacity: List<Long> = emptyList(),

    @get:PropertyName("department") @set:PropertyName("department")
    var department: String = "",

    @get:PropertyName("startTime") @set:PropertyName("startTime")
    var startTime: String = "08:00",

    @get:PropertyName("endTime") @set:PropertyName("endTime")
    var endTime: String = "22:00",

    var dailyBreakHours: List<Int> = emptyList(),

    @get:PropertyName("duration") @set:PropertyName("duration")
    var bookedSlots: Map<String, List<Int>> = emptyMap(),

    @get:PropertyName("specialClosures") @set:PropertyName("specialClosures")
    var specialClosures: Map<String, List<Int>> = emptyMap()

)

//Am2234
//ThuthuCar2

//Ah3234
//HajimiNanBei3

//Ak4234
//imKwa*2