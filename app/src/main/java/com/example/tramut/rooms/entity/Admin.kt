package com.example.myfacilitybookingsystem.rooms.entity

import com.google.firebase.firestore.PropertyName

data class AdminUser(
    val id: String = "",

    // CHANGE 'val' TO 'var' HERE
    @get:PropertyName("admin_id") @set:PropertyName("admin_id")
    var login_id: String = "",

    @get:PropertyName("admin_name") @set:PropertyName("admin_name")
    var name: String = "",

    @get:PropertyName("admin_email") @set:PropertyName("admin_email")
    var email: String = "",

    @get:PropertyName("admin_password") @set:PropertyName("admin_password")
    var password: String = "",

    var department: String = ""
)