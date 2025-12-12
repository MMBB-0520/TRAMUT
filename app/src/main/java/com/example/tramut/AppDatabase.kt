package com.example.myfacilitybookingsystem

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myfacilitybookingsystem.rooms.dao.UsersDAO
import com.example.myfacilitybookingsystem.rooms.entity.Users
import kotlin.jvm.java

@Database(
    entities = [
        Users::class
    ],
    version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDAO
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        db.execSQL("PRAGMA foreign_keys=ON;")
                    }
                })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

