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
        userId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = BookingUIState.Loading

                // 1. Get all physical courts (e.g., Badminton Court 1, Court 2) from 'facilities' collection
                val facilitiesSnapshot = firestore.collection("facilities")
                    .whereEqualTo("category", venueCategory)
                    .get()
                    .await()

                val allCourts = facilitiesSnapshot.documents.mapNotNull { it.getString("name") }

                // 2. Get all current bookings for that date and time
                val bookingsSnapshot = firestore.collection("bookings")
                    .whereEqualTo("date", date)
                    .whereEqualTo("startTime", "$hour:00")
                    .get()
                    .await()

                val takenCourts = bookingsSnapshot.documents.mapNotNull { doc ->
                    val status = doc.getString("status") ?: ""
                    // Exclude cancelled bookings so the court becomes available again
                    if (status.lowercase() != "cancelled") doc.getString("finalVenue") else null
                }

                // 3. LOGIC: Find the first court name that is NOT in the taken list
                val assignedCourtName = allCourts.firstOrNull { it !in takenCourts }

                if (assignedCourtName != null) {
                    // 4. Create the final Booking object
                    val bookingId = UUID.randomUUID().toString()
                    val finalBooking = Booking(
                        bookingId = bookingId,
                        bookingNo = bookingId.take(8).uppercase(), // Shortened ID for UI
                        userId = userId,
                        facility = department,
                        venue = venueCategory,
                        finalVenue = assignedCourtName, // The System-Assigned specific court
                        date = date,
                        startTime = "$hour:00",
                        endTime = "${hour + 1}:00",
                        status = "confirmed",
                        level = getLevelForVenue(venueCategory),
                        building = getBuildingForVenue(venueCategory)
                    )

                    // 5. Save to Firestore
                    firestore.collection("bookings").document(bookingId).set(finalBooking).await()

                    _uiState.value = BookingUIState.Success
                    onComplete(true, "Successfully booked $assignedCourtName")
                } else {
                    _uiState.value = BookingUIState.Error("Fully Booked")
                    onComplete(false, "No courts available for this slot.")
                }
            } catch (e: Exception) {
                _uiState.value = BookingUIState.Error(e.message ?: "Unknown error")
                onComplete(false, e.message ?: "Error")
            }
        }
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