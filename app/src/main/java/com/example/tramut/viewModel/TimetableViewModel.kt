package com.example.myfacilitybookingsystem.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.TimetableRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// UI State remains the same
data class TimetableUiState(
    val selectedDate: String = "",
    val facilitiesList: List<Facility> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bookingsList: List<Booking> = emptyList()
)

class TimetableViewModel(
    private val repository: TimetableRepository = TimetableRepository()
) : ViewModel() {

    private val db = Firebase.firestore
    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState = _uiState.asStateFlow()

    private var facilityListener: ListenerRegistration? = null

    init {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)
        _uiState.update { it.copy(selectedDate = today) }
    }

    override fun onCleared() {
        super.onCleared()
        facilityListener?.remove()
    }

    fun fetchTimetableData(identifier: String, isCategory: Boolean, date: String) {
        facilityListener?.remove()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val facilitiesRef = db.collection("facilities")
        val query = if (isCategory) {
            facilitiesRef.whereEqualTo("category", identifier)
        } else {
            facilitiesRef.whereEqualTo("department", identifier)
        }

        facilityListener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val liveList = snapshot.documents.mapNotNull { doc ->

                    // --- 1. Robust Capacity Mapping ---
                    val capacityData = doc.get("capacity")
                    val capacityList: List<Long> = when (capacityData) {
                        is List<*> -> capacityData.filterIsInstance<Long>()
                        is Long -> listOf(capacityData)
                        else -> emptyList()
                    }

                    // --- 2. Robust Daily Break Hours Mapping ---
                    val breakHoursData = doc.get("dailyBreakHours")
                    val breakHoursList: List<Int> = when (breakHoursData) {
                        is List<*> -> breakHoursData.mapNotNull { (it as? Long)?.toInt() ?: (it as? Int) }
                        is String -> breakHoursData.split(",").mapNotNull { it.trim().toIntOrNull() }
                        else -> emptyList()
                    }

                    // --- 3. CRITICAL: Robust Special Closures Mapping (Fix for Date Range) ---
                    // Firestore stores numbers as Long. We must convert them to Int for .contains(hour) to work.
                    val rawSpecialClosures = doc.get("specialClosures") as? Map<String, Any> ?: emptyMap()
                    val convertedSpecialClosures = rawSpecialClosures.mapValues { entry ->
                        val hoursList = entry.value as? List<*>
                        hoursList?.mapNotNull {
                            when(it) {
                                is Long -> it.toInt()
                                is Int -> it
                                else -> null
                            }
                        } ?: emptyList<Int>()
                    }

                    Facility(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        department = doc.getString("department") ?: "",
                        category = doc.getString("category") ?: "",
                        startTime = doc.getString("startTime") ?: "08:00",
                        endTime = doc.getString("endTime") ?: "22:00",
                        capacity = capacityList,
                        dailyBreakHours = breakHoursList,
                        specialClosures = convertedSpecialClosures // Used converted map
                    )
                }

                _uiState.update { it.copy(facilitiesList = liveList, isLoading = false) }
                loadBookingsForDate(date)
            }
        }
    }

    fun loadBookingsForDate(date: String) {
        viewModelScope.launch {
            try {
                val allBookings = repository.getBookingsForDate(date)
                _uiState.update { it.copy(bookingsList = allBookings) }
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error loading bookings", e)
            }
        }
    }

    fun updateDate(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        loadBookingsForDate(newDate)
    }

    fun getSlotStatus(facility: Facility, hour: Int): String {
        val dateString = uiState.value.selectedDate

        val facilityStartHour = facility.startTime.split(":")[0].toIntOrNull() ?: 8
        val facilityEndHour = facility.endTime.split(":")[0].toIntOrNull() ?: 22

        // 1. Operational Hours
        if (hour < facilityStartHour || hour >= facilityEndHour) return "Closed"

        // 2. Daily Maintenance
        if (facility.dailyBreakHours.contains(hour)) return "Maintenance"

        val specialClosedHours = facility.specialClosures[dateString]
        if (specialClosedHours != null && specialClosedHours.contains(hour)) {
            return "Maintenance"
        }

        // 4. Booking Check
        val isBooked = uiState.value.bookingsList.any { booking ->
            val bookedStartHour = booking.startTime.split(":")[0].toIntOrNull() ?: 0
            val bookedEndHour = booking.endTime.split(":")[0].toIntOrNull() ?: 0

            booking.venue == facility.name &&
                    booking.bookingDate == dateString &&
                    bookedStartHour <= hour &&
                    bookedEndHour > hour
        }

        return if (isBooked) "Booked" else "Available"
    }
}