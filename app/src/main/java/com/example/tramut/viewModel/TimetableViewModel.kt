package com.example.myfacilitybookingsystem.viewModel

import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Booking
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.rooms.repo.FacilityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class TimetableViewModel(
    private val repository: FacilityRepository = FacilityRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState = _uiState.asStateFlow()

    var currentBookings = mutableStateListOf<Booking>()
        private set

    init {
        // Set default date to today
        val today = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.now().toString()
        } else "2025-01-01"
        _uiState.update { it.copy(selectedDate = today) }
    }

    // --- 1. LOAD FACILITY ---
    fun loadFacility(facilityId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getFacilityById(facilityId) { facility ->
                if (facility != null) {
                    _uiState.update { it.copy(currentFacility = facility) }
                    // Immediately load bookings for the default date
                    loadBookingsForDate(facility.id, _uiState.value.selectedDate)
                }
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // --- 2. UPDATE DATE ---
    fun updateDate(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        val facilityId = _uiState.value.currentFacility?.id
        if (facilityId != null) {
            loadBookingsForDate(facilityId, newDate)
        }
    }

    // --- 3. LOAD BOOKINGS HELPER ---
    // Fixed: This function is now correctly inside the class braces
    private fun loadBookingsForDate(facilityId: String, date: String) {
        repository.getBookings(facilityId, date) { retrievedBookings ->
            // Clear old data and add new data to trigger UI refresh
            currentBookings.clear()
            currentBookings.addAll(retrievedBookings)
        }
    }

    // --- 4. CREATE BOOKING ---
    fun bookSlot(hour: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val facility = uiState.value.currentFacility ?: return
        val date = uiState.value.selectedDate

        // This object structure must match your Booking data class constructor
        val newBooking = Booking(
            id = UUID.randomUUID().toString(),
            facilityId = facility.id,
            timeSlot = hour,
            date = date,
            bookingNo = "BK-${System.currentTimeMillis().toString().takeLast(6)}",
            facility = facility.name,
            venue = facility.name,
            building = "Main Block",
            level = "Level 1",
            duration = "1 Hour",
            checkIn = String.format("%02d:00", hour),
            checkOut = String.format("%02d:00", hour + 1),
            status = "Confirmed"
        )

        viewModelScope.launch {
            // Fixed: Calls repo correctly inside coroutine
            val success = repository.saveBooking(newBooking)
            if (success) {
                // Refresh grid to show Blue immediately
                loadBookingsForDate(facility.id, date)
                onSuccess()
            } else {
                onError("Failed to save booking.")
            }
        }
    }

    // --- 5. GRID STATUS LOGIC ---
    fun getSlotStatus(hour: Int): String {
        val facility = uiState.value.currentFacility ?: return "Loading"
        val date = uiState.value.selectedDate

        // A. Check Operating Hours (Gray)
        val openHour = facility.startTime.split(":").firstOrNull()?.toIntOrNull() ?: 8
        val closeHour = facility.endTime.split(":").firstOrNull()?.toIntOrNull() ?: 22
        if (hour < openHour || hour >= closeHour) return "Closed"

        // B. Check Maintenance (Red)
        val closedHours = facility.specialClosures[date]
        if (closedHours != null && closedHours.contains(hour)) return "Maintenance"

        // C. Check Bookings (Blue)
        if (currentBookings.any { it.timeSlot == hour }) return "Booked"

        // D. Default (Green)
        return "Available"
    }
}

// Simple State Holder
data class TimetableUiState(
    val selectedDate: String = "",
    val currentFacility: Facility? = null,
    val isLoading: Boolean = false
)