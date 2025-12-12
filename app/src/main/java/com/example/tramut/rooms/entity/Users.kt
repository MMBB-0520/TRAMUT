package com.example.myfacilitybookingsystem.rooms.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class Users(
    @PrimaryKey
    @ColumnInfo(name = "loginId")
    val loginId: String = "",

    @ColumnInfo(name = "username")
    val username: String = "",

    @ColumnInfo(name = "email")
    val email: String = "",

    @ColumnInfo(name = "IC")
    val userIC: String = "",

    @ColumnInfo(name = "role")
    val role: String = ""

)
