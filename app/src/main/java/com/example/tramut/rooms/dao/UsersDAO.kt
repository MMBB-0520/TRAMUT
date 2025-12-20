package com.example.tramut.rooms.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.tramut.rooms.entity.Users
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: Users)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<Users>)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<Users>>

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("SELECT * FROM users WHERE loginId = :loginId")
    suspend fun getUserByLoginId(loginId: String): Users?

    @Query("SELECT * FROM users WHERE loginId = :loginId AND role = :role")
    suspend fun getUserByLoginIdAndRole(loginId: String, role: String): Users?

    @Transaction
    suspend fun replaceAllUsers(users: List<Users>) {
        deleteAllUsers()
        insertUsers(users)
    }
}