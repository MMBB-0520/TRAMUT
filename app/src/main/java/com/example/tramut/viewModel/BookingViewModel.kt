package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.entity.Member
import com.example.tramut.rooms.repo.BookingRepo
import com.example.tramut.rooms.repo.TimetableRepository
import com.example.tramut.userInterface.formatDateForDisplay
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

    fun generate9UniqueDigits(): String {
        return (0..9)
            .map { (0..9).random() }
            .take(9)
            .joinToString("")
    }
    fun LCode(): String {
        return "L${generate9UniqueDigits()}"
    }

    fun CCode(): String {
        return "C${generate9UniqueDigits()}"
    }

    fun SCode(): String {
        return "S${generate9UniqueDigits()}"
    }

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings

    fun fetchAllBookingsByDate(date: String) {
        firestore.collection("bookings")
            .whereEqualTo("date", date) // Get everyone's bookings for this day
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _allBookings.value = snapshot.toObjects(Booking::class.java)
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
                            bookingNo = doc.getString("bookingNo") ?: doc.getString("bookingId") ?: doc.id
                        )
                    }
                    _bookingList.value = bookings
                } else {
                    _bookingList.value = emptyList()
                }
            }
    }

    private fun parseTo24Hour(timeStr: String): Int {
        // Expected format: "04:00 PM" or "16:00"
        val isPM = timeStr.uppercase().contains("PM")
        val hourPart = timeStr.split(":")[0].trim().toIntOrNull() ?: 0

        return when {
            isPM && hourPart < 12 -> hourPart + 12
            !isPM && hourPart == 12 -> 0
            isPM && hourPart == 12 -> 12
            else -> hourPart
        }
    }

    fun manualAutoAssignAndSave(
        category: String,
        date: String,
        startTime: String,
        endTime: String,
        userId: String,
        pax: Int,
        members: List<Member>,
        level: String,
        building: String,
        onResult: (Boolean, String) -> Unit,
    ) {
        viewModelScope.launch {
            val firestoreDate = formatDateForDisplay(date)
            val startH = parseTo24Hour(startTime)
            val endH = parseTo24Hour(endTime)
            val requestedHours = (startH until endH).toList()

            // CHANGE THIS LINE: Use the new repo function with pax
            val facilities = repo.getFacilitiesByCategoryAndCapacity(category, pax)
            val existingBookings = repo.getBookingsByDate(firestoreDate)

            // Find the first facility in the size-filtered list that is not overlapped
            val availableFacility = facilities.find { facility ->
                val bookingsForThisFacility = existingBookings.filter {
                    it.finalVenue.trim().equals(facility.name.trim(), ignoreCase = true)
                }
                val isOverlap = bookingsForThisFacility.any { b ->
                    b.hoursList.any { it in requestedHours } && b.status != "Cancelled"
                }
                !isOverlap
            }

            if (availableFacility != null) {
                val newBooking = Booking(
                    bookingId = firestore.collection("bookings").document().id,
                    userId = userId,
                    timeslotId = "", // ADDED: Provide empty string if not used
                    facility = category,
                    venue = category,
                    level = level,
                    building = building,
                    duration = "$startTime - $endTime",
                    startTime = startTime,
                    endTime = endTime,
                    checkIn = "",  // ADDED: Missing field
                    checkOut = "", // ADDED: Missing field
                    bookingNo = LCode(),
                    members = members,
                    status = "Confirmed",
                    date = firestoreDate, // Using the formatted date for Blue status
                    finalVenue = availableFacility.name,
                    hoursList = requestedHours,
                    pax = pax
                )

                // 5. Save and Return Result
                val success = repo.saveBooking(newBooking)
                if (success) {
                    onResult(true, "Assigned to ${availableFacility.name}")
                } else {
                    onResult(false, "Failed to save booking.")
                }
            } else {
                onResult(false, "No available courts for this slot.")
            }
        }
    }



    private val repo = BookingRepo()


    // 取消预订的方法
    suspend fun cancelBooking(bookingNo: String): Boolean {
        return try {
            _uiState.value = BookingUIState.Loading

            // 使用 bookingNo 字段查找
            val querySnapshot = firestore.collection("bookings")
                .whereEqualTo("bookingId", bookingNo)  // 改为 bookingNo
                .limit(1)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val document = querySnapshot.documents[0]
                val documentId = document.id

                // 更新状态为"cancelled"
                val updates = hashMapOf<String, Any>(
                    "status" to "Cancelled",
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
    fun cancelBookingWithScope(bookingNo: String) {
        viewModelScope.launch {
            cancelBooking(bookingNo)
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