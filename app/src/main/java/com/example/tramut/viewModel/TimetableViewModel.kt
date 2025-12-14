package com.example.myfacilitybookingsystem.viewModel

import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.TimetableRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// 1. Updated Data Class (Added errorMessage)
data class TimetableUiState(
    val selectedDate: String = "",
    val facilitiesList: List<Facility> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TimetableViewModel(
    private val repository: TimetableRepository = TimetableRepository()
) : ViewModel() {

    // 2. FIXED: Initialized Firestore
    private val db = Firebase.firestore

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState = _uiState.asStateFlow()

    // Holds all bookings for the ENTIRE department for the selected date
    var currentBookings = mutableStateListOf<Booking>()
        private set

    init {
        // Set default date to Today
        val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else "2025-01-01"

        _uiState.update { it.copy(selectedDate = today) }

        // Load bookings for today immediately
        loadBookingsForDate(today)
    }

    // --- 1. LOAD ALL FACILITIES (REAL-TIME LISTENER) ---
    fun loadFacilities(department: String) {
        _uiState.update { it.copy(isLoading = true) }

        db.collection("facilities")
            .whereEqualTo("department", department)
            // Removed status filter so "broken" facilities still appear
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? -> // 3. Explicit Types

                if (error != null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val liveList = snapshot.documents.mapNotNull { doc ->
                        try {
                            Facility(
                                id = doc.id,
                                name = doc.getString("name") ?: "Unknown",
                                department = doc.getString("department") ?: "",
                                status = doc.getString("status") ?: "Available",
                                startTime = doc.getString("startTime") ?: "08:00",
                                endTime = doc.getString("endTime") ?: "22:00",
                                specialClosures = try {
                                    @Suppress("UNCHECKED_CAST")
                                    doc.get("specialClosures") as? Map<String, List<Int>> ?: emptyMap()
                                } catch (e: Exception) { emptyMap() }
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _uiState.update {
                        it.copy(facilitiesList = liveList, isLoading = false)
                    }
                }
            }
    }

    // --- 2. UPDATE DATE ---
    fun updateDate(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        // Refresh bookings when date changes
        loadBookingsForDate(newDate)
    }

    // --- 3. LOAD BOOKINGS ---
    fun loadBookingsForDate(date: String) {
        viewModelScope.launch {
            try {
                // Get ALL bookings for this date
                val allBookings = repository.getBookingsForDate(date)

                // Update the state list
                currentBookings.clear()
                currentBookings.addAll(allBookings)
            } catch (e: Exception) {
                // Handle error if repo fails
            }
        }
    }

    // --- 4. CHECK SLOT STATUS ---
    fun getSlotStatus(facility: Facility, hour: Int): String {
        val date = uiState.value.selectedDate

        // 1. Check Opening Hours
        val openHour = facility.startTime.split(":").firstOrNull()?.toIntOrNull() ?: 8
        val closeHour = facility.endTime.split(":").firstOrNull()?.toIntOrNull() ?: 22

        if (hour < openHour || hour >= closeHour) return "Closed"

        // 2. Check Maintenance
        val closedHours = facility.specialClosures[date]
        if (closedHours != null && closedHours.contains(hour)) return "Maintenance"

        // 3. Check Bookings
        val hourPrefix = String.format("%02d", hour)

        val isBooked = currentBookings.any { booking ->
            booking.facility == facility.name &&
                    // Check if the booking starts at this hour
                    (booking.startTime.startsWith(hourPrefix) || booking.checkIn.startsWith(hourPrefix))
        }

        if (isBooked) return "Booked"

        return "Available"
    }
}