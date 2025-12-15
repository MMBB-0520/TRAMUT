package com.example.tramut.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.example.tramut.rooms.entity.Booking

@Dao
interface CheckDao {

    // 1. Get the booking to validate it
    @Query("SELECT * FROM Booking WHERE bookingId = :id")
    suspend fun getBookingById(id: String): Booking?

    // 2. Update Check-In Time
    @Query("UPDATE Booking SET checkIn = :time, status = 'Checked In' WHERE bookingId = :id")
    suspend fun updateCheckIn(id: String, time: String)

    // 3. Update Check-Out Time
    @Query("UPDATE Booking SET checkOut = :time, status = 'Completed' WHERE bookingId = :id")
    suspend fun updateCheckOut(id: String, time: String)
}