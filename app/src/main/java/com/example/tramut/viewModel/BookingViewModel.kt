package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log

class MyBookingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // StateFlow 用于 Compose UI 监听
    private val _bookingList = MutableStateFlow<List<Booking>>(emptyList())
    val bookingList: StateFlow<List<Booking>> = _bookingList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // UI 状态
    private val _uiState = MutableStateFlow<BookingUIState>(BookingUIState.Idle)
    val uiState: StateFlow<BookingUIState> = _uiState

    private var listenerRegistration: ListenerRegistration? = null

    // ===============================
    // Listen booking list
    // ===============================
    fun startListening(userId: String) {
        _isLoading.value = true
        stopListening()

        listenerRegistration = firestore
            .collection("bookings")
            .whereEqualTo("userId", userId)
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _uiState.value =
                        BookingUIState.Error(error.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(
                            // ✅ 统一：UI 用 bookingNo = bookingId
                            bookingNo = doc.getString("bookingId") ?: doc.id
                        )
                    }
                    _bookingList.value = bookings
                } else {
                    _bookingList.value = emptyList()
                }
            }
    }

    // ===============================
    // Cancel booking (by bookingId)
    // ===============================
    suspend fun cancelBooking(bookingId: String): Boolean {
        return try {
            _uiState.value = BookingUIState.Loading
            Log.d("CANCEL", "Try cancel bookingId=$bookingId")

            val querySnapshot = firestore
                .collection("bookings")
                .whereEqualTo("bookingId", bookingId) // ✅ 核心
                .limit(1)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                _uiState.value = BookingUIState.Error("Booking not found")
                return false
            }

            val documentId = querySnapshot.documents.first().id

            val updates = hashMapOf<String, Any>(
                "status" to "Cancelled",
                "cancelledAt" to System.currentTimeMillis()
            )

            firestore.collection("bookings")
                .document(documentId)
                .update(updates)
                .await()

            _uiState.value = BookingUIState.Success
            true

        } catch (e: Exception) {
            _uiState.value =
                BookingUIState.Error(e.message ?: "Failed to cancel booking")
            false
        }
    }

    // Compose 调用用
    fun cancelBookingWithScope(bookingId: String) {
        viewModelScope.launch {
            cancelBooking(bookingId)
        }
    }

    fun resetUIState() {
        _uiState.value = BookingUIState.Idle
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    // ===============================
    // Venue helpers (原样保留)
    // ===============================
    fun getLevelForVenue(venue: String): String {
        val venueLower = venue.lowercase()
        return when {
            venueLower.contains("badminton") ||
                    venueLower.contains("squash") ||
                    venueLower.contains("gym") ||
                    venueLower.contains("swimming") ||
                    venueLower.contains("snooker") ||
                    venueLower.contains("pickleball") ||
                    venueLower.contains("tennis") ||
                    venueLower.contains("futsal") -> "Ground Floor"

            venueLower.contains("table tennis") -> "First Floor"

            venueLower.contains("karaoke") ||
                    venueLower.contains("guest") -> "Second Floor"

            venueLower.contains("library") -> {
                when {
                    venueLower.contains("discussion") -> "1A"
                    else -> "1"
                }
            }

            venueLower.contains("individual study") -> "2A"

            venueLower.contains("citc") ||
                    venueLower.contains("cyber") ||
                    venueLower.contains("pc") ||
                    venueLower.contains("projector") -> "Second Floor"

            venueLower.contains("discussion") -> {
                when {
                    venueLower.contains("library") -> "1A"
                    venueLower.contains("cyber") -> "First Floor"
                    else -> "Not specified"
                }
            }
            else -> "Not specified"
        }
    }

    fun getBuildingForVenue(venue: String): String {
        val venueLower = venue.lowercase()
        return when {
            venueLower.contains("badminton") ||
                    venueLower.contains("squash") ||
                    venueLower.contains("gym") ||
                    venueLower.contains("swimming") ||
                    venueLower.contains("snooker") ||
                    venueLower.contains("pickleball") ||
                    venueLower.contains("table tennis") ||
                    venueLower.contains("tennis") ||
                    venueLower.contains("futsal") ||
                    venueLower.contains("karaoke") ||
                    venueLower.contains("guest") -> "Sports Complex"

            venueLower.contains("library") ||
                    venueLower.contains("individual study") -> "Library"

            venueLower.contains("citc") ||
                    venueLower.contains("cyber") -> "Cyber Centre"

            venueLower.contains("discussion") -> {
                when {
                    venueLower.contains("library") -> "Library"
                    venueLower.contains("cyber") -> "Cyber Centre"
                    else -> "General Building"
                }
            }
            else -> "Not specified"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // ===============================
    // UI State
    // ===============================
    sealed class BookingUIState {
        object Idle : BookingUIState()
        object Loading : BookingUIState()
        object Success : BookingUIState()
        data class Error(val message: String) : BookingUIState()
    }
}