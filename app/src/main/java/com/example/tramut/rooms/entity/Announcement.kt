package com.example.myfacilitybookingsystem.rooms.entity

import com.google.firebase.firestore.PropertyName

data class Announcement(
    val id: String = "",

    // 2. Main Content
    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("content") @set:PropertyName("content")
    var content: String = "",

    @get:PropertyName("department") @set:PropertyName("department")
    var department: String = "",

    @get:PropertyName("admin_id") @set:PropertyName("admin_id")
    var admin_id: String = "",

    @get:PropertyName("announcement_status") @set:PropertyName("announcement_status")
    var announcement_status: String = "Active",

    @get:PropertyName("created_date_str") @set:PropertyName("created_date_str")
    var created_date_str: String = "",

    @get:PropertyName("expiry_date_str") @set:PropertyName("expiry_date_str")
    var expiry_date_str: String = "",

    @get:PropertyName("venue") @set:PropertyName("venue")
    var venue: String = ""
)