package com.example.myfacilitybookingsystem.viewModel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.TimetableRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// UI State
data class TimetableUiState(
    val selectedDate: String = "",
    val facilitiesList: List<Facility> = emptyList(),
    val categoryList: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val bookingsList: List<Booking> = emptyList() // Now correctly managed by the VM
)

class TimetableViewModel(
    private val repository: TimetableRepository = TimetableRepository()
) : ViewModel() {

    private val db = Firebase.firestore
    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState = _uiState.asStateFlow()

    // NOTE: currentBookings is redundant if you use uiState.bookingsList.
    // It's kept here because it was in your original code, but you should prefer uiState.
    var currentBookings = mutableStateListOf<Booking>()
        private set

    init {
        // Safe Date Initialization
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Calendar.getInstance().time)

        _uiState.update { it.copy(selectedDate = today) }

        // Load bookings for today immediately
        loadBookingsForDate(today)
    }

    // --- 2. LOAD FACILITIES ---
    fun loadFacilities(identifier: String, isCategory: Boolean) {
        _uiState.update { it.copy(isLoading = true) }

        val facilitiesRef = db.collection("facilities")
        val query = if (isCategory) {
            facilitiesRef.whereEqualTo("category", identifier)
        } else {
            facilitiesRef.whereEqualTo("department", identifier)
        }

        // Use addSnapshotListener for real-time updates (which is crucial)
        query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                _uiState.update { it.copy(isLoading = false, errorMessage = error.message) }
                return@addSnapshotListener
            }

            if (snapshot != null) {
                android.util.Log.d("DEBUG_TIMETABLE", "Found ${snapshot.size()} facilities for '$identifier'")

                val liveList = snapshot.documents.mapNotNull { doc ->

                    // --- 1. Capacity List ---
                    val capacityData = doc.get("capacity")
                    val capacityList: List<Long> = when (capacityData) {
                        is List<*> -> capacityData.filterIsInstance<Long>()
                        is Long -> listOf(capacityData)
                        else -> emptyList()
                    }

                    // --- 2. Daily Break Hours (Fix: Robust Long/Int Conversion) ---
                    val breakHoursData = doc.get("dailyBreakHours")
                    val breakHoursList: List<Int> = when (breakHoursData) {
                        is List<*> -> breakHoursData.filterIsInstance<Long>().map { it.toInt() } // Handles List<Long> from Firestore
                        is String -> breakHoursData.split(",").mapNotNull { it.trim().toIntOrNull() }
                        else -> emptyList()
                    }

                    // --- 3. Booked Slots and Closures (Fix: Removed duplicate declarations) ---
                    @Suppress("UNCHECKED_CAST")
                    val bookedSlotsMap = doc.get("bookedSlots") as? Map<String, List<Int>> ?: emptyMap()

                    @Suppress("UNCHECKED_CAST")
                    val specialClosuresMap = doc.get("specialClosures") as? Map<String, List<Int>> ?: emptyMap()

                    Facility(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        department = doc.getString("department") ?: "",
                        category = doc.getString("category") ?: "",
                        startTime = doc.getString("startTime") ?: "08:00",
                        endTime = doc.getString("endTime") ?: "22:00",
                        capacity = capacityList,
                        dailyBreakHours = breakHoursList,
                        bookedSlots = bookedSlotsMap,
                        specialClosures = specialClosuresMap
                    )
                }
                _uiState.update {
                    it.copy(facilitiesList = liveList, isLoading = false)
                }
            }
        }
    }

    // --- 3. UPDATE DATE ---
    fun updateDate(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        loadBookingsForDate(newDate)
    }

    // --- 4. LOAD BOOKINGS (Fix: Update UI State) ---
    fun loadBookingsForDate(date: String) {
        viewModelScope.launch {
            try {
                // Fetch bookings from your repository
                val allBookings = repository.getBookingsForDate(date)

                // Redundant list update
                currentBookings.clear()
                currentBookings.addAll(allBookings)

                // ✅ CRITICAL FIX: Update the UI State with the fetched bookings
                _uiState.update { it.copy(bookingsList = allBookings) }
            } catch (e: Exception) {
                Log.e("TimetableViewModel", "Error loading bookings", e)
            }
        }
    }

    // --- 5. CHECK SLOT STATUS (Core Logic is correct now) ---
    fun getSlotStatus(facility: Facility, hour: Int): String {
        val dateString = uiState.value.selectedDate

        val facilityStartHour = facility.startTime.split(":")[0].toIntOrNull() ?: 8
        val facilityEndHour = facility.endTime.split(":")[0].toIntOrNull() ?: 22

        // 1. Closed (Outside operational hours)
        if (hour < facilityStartHour || hour >= facilityEndHour) {
            return "Closed"
        }

        // 2. Maintenance (Daily Breaks)
        if (facility.dailyBreakHours.contains(hour)) {
            return "Maintenance" // Red
        }

        // 3. Maintenance (Special Closures)
        val specialClosedHours = facility.specialClosures[dateString]
        if (specialClosedHours != null && specialClosedHours.contains(hour)) {
            return "Maintenance" // Red
        }

        // 4. Booked
        val facilityMatchName = facility.name

        // Checks the updated uiState.bookingsList (now correctly populated by loadBookingsForDate)
        val isBooked = uiState.value.bookingsList.any { booking ->
            val bookedStartHour = booking.startTime.split(":")[0].toIntOrNull() ?: 0
            val bookedEndHour = booking.endTime.split(":")[0].toIntOrNull() ?: 0

            booking.venue == facilityMatchName &&
                    booking.bookingDate == dateString &&
                    bookedStartHour <= hour &&
                    bookedEndHour > hour
        }

        if (isBooked) {
            return "Booked" // Blue
        }

        // 5. Available (Default)
        return "Available" // Green
    }
}