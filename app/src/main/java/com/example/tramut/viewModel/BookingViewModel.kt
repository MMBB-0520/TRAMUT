package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tramut.rooms.entity.Booking
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MyBookingViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    // StateFlow 用于 Compose UI 监听
    private val _bookingList = MutableStateFlow<List<Booking>>(emptyList())
    val bookingList: StateFlow<List<Booking>> = _bookingList

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 添加UI状态管理
    private val _uiState = MutableStateFlow<BookingUIState>(BookingUIState.Idle)
    val uiState: StateFlow<BookingUIState> = _uiState

    private var listenerRegistration: ListenerRegistration? = null

    fun performSystemAssignment(
        department: String,
        venueCategory: String,
        date: String,
        hour: Int,
        pax: Long,
        userId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        val db = Firebase.firestore

        db.collection("facilities")
            .whereEqualTo("department", department)
            .whereEqualTo("category", venueCategory)
            .whereArrayContains("capacity", pax) // This matches the [4, 5, 6] list
            .get()
            .addOnSuccessListener { facilityDocs ->
                if (facilityDocs.isEmpty) {
                    onComplete(false, "No room in $venueCategory fits $pax pax.")
                    return@addOnSuccessListener
                }

                val matchingIds = facilityDocs.map { it.id }

                db.collection("bookings")
                    .whereEqualTo("date", date)
                    .whereEqualTo("hour", hour)
                    .whereIn("facilityId", matchingIds)
                    .get()
                    .addOnSuccessListener { bookingDocs ->
                        val takenRoomIds = bookingDocs.mapNotNull { it.getString("facilityId") }

                        val finalVenueId = matchingIds.firstOrNull { it !in takenRoomIds }

                        if (finalVenueId != null) {
                            saveBooking(finalVenueId, date, hour, userId, onComplete)
                        } else {
                            onComplete(false, "All $venueCategory rooms for $pax pax are fully booked.")
                        }
                    }
            }
            .addOnFailureListener { e ->
                onComplete(false, "Error: ${e.message}")
            }
    }

    private fun saveBooking(facilityId: String, date: String, hour: Int, userId: String, onComplete: (Boolean, String) -> Unit) {
        val bookingData = hashMapOf(
            "facilityId" to facilityId,
            "date" to date,
            "hour" to hour,
            "userId" to userId,
            "status" to "Confirmed"
        )
        Firebase.firestore.collection("bookings").add(bookingData)
            .addOnSuccessListener { onComplete(true, "Success") }
            .addOnFailureListener { onComplete(false, "Failed to save booking") }
    }

    fun startListening(userId: String) {
        _isLoading.value = true

        // 停止之前的监听器
        stopListening()

        listenerRegistration = firestore
            .collection("bookings")
            .whereEqualTo("userId", userId)
            // 可以根据需要添加排序
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false

                if (error != null) {
                    _uiState.value = BookingUIState.Error(error.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val bookings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(
                            // 确保 bookingNo 有值
                            bookingNo = doc.getString("bookingNo") ?: doc.getString("bookingId") ?: doc.id
                        )
                    }
                    _bookingList.value = bookings
                } else {
                    _bookingList.value = emptyList()
                }
            }
    }

    // 取消预订的方法
    suspend fun cancelBooking(bookingId: String): Boolean {
        return try {
            _uiState.value = BookingUIState.Loading

            // 使用 bookingNo 字段查找
            val querySnapshot = firestore.collection("bookings")
                .whereEqualTo("bookingNo", bookingId)  // 改为 bookingNo
                .limit(1)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents[0]
                val documentId = document.id

                // 更新状态为"cancelled"
                val updates = hashMapOf<String, Any>(
                    "status" to "cancelled",
                    // 可选：添加取消时间
                    "cancelledAt" to System.currentTimeMillis()
                )

                firestore.collection("bookings").document(documentId)
                    .update(updates)
                    .await()

                _uiState.value = BookingUIState.Success
                true
            } else {
                _uiState.value = BookingUIState.Error("Booking not found")
                false
            }
        } catch (e: Exception) {
            _uiState.value = BookingUIState.Error(e.message ?: "Failed to cancel booking")
            false
        }
    }



    // ViewModelScope封装的方法，方便在Compose中调用
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
                    else -> "1"  // 主图书馆在1楼
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


            venueLower.contains("library") -> "Library"
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

            // 默认
            else -> "Not specified"
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    // UI状态密封类
    sealed class BookingUIState {
        object Idle : BookingUIState()
        object Loading : BookingUIState()
        object Success : BookingUIState()
        data class Error(val message: String) : BookingUIState()
    }
}