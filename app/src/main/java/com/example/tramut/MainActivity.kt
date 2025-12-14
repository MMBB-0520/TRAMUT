package com.example.tramut

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.myfacilitybookingsystem.AppDatabase
import com.example.myfacilitybookingsystem.FBSApp
import com.example.myfacilitybookingsystem.rooms.repo.UsersRepo
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.TRAMUTTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        val db = AppDatabase.getInstance(this)
        val usersRepo = UsersRepo(db.usersDao())
        setContent {
            TRAMUTTheme {
                FBSApp(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Background),
                    usersRepo = usersRepo
                )
            }
        }
    }

}