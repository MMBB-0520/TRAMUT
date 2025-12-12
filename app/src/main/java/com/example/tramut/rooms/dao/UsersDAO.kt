package com.example.myfacilitybookingsystem.rooms.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.myfacilitybookingsystem.rooms.entity.Users
import kotlinx.coroutines.flow.Flow

@Dao
interface UsersDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: Users)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<Users>)

    @Update
    suspend fun updateUser(user: Users)

    @Update
    suspend fun updateUsers(users: List<Users>)

    @Delete
    suspend fun deleteUser(user: Users)

    @Delete
    suspend fun deleteUsers(users: List<Users>)

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