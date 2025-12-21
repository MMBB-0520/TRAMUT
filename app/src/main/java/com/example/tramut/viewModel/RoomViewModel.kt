package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import com.example.tramut.rooms.entity.Room
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoomViewModel : ViewModel() {

    private val _rooms = MutableStateFlow<List<Room>>(emptyList())
    val rooms: StateFlow<List<Room>> = _rooms.asStateFlow()

    fun observeRooms(category: String) {
        Firebase.firestore
            .collection("facilities")
            .document(category)
            .collection("rooms")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _rooms.value = emptyList()
                    return@addSnapshotListener
                }

                _rooms.value = snapshot?.documents
                    ?.mapNotNull { it.toObject(Room::class.java) }
                    ?: emptyList()
            }
    }
}

