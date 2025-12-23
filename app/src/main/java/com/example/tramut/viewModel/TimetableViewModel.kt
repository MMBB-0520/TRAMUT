package com.example.tramut.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.rooms.repo.FacilityRepository
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.TimetableRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class TimetableUiState(
    val selectedDate: String = "",
    val facilitiesList: List<Facility> = emptyList(),
    val allBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class TimetableViewModel(
    private val repository: TimetableRepository = TimetableRepository()
) : ViewModel() {

    private val db = Firebase.firestore
    private val _uiState = MutableStateFlow(TimetableUiState())
    val uiState = _uiState.asStateFlow()


    private var facilityListener: ListenerRegistration? = null

    init {
        val sdf = SimpleDateFormat("dd / MMM / yyyy (EEE)", Locale.ENGLISH)
        val today = sdf.format(Calendar.getInstance().time)
        _uiState.update { it.copy(selectedDate = today) }
    }

    override fun onCleared() {
        super.onCleared()
        facilityListener?.remove()
        bookingsListener?.remove()
    }

    // In TimetableViewModel.kt

    // 1. Add this state to hold real-time bookings
    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings
    private val facilityRepo = FacilityRepository()

    // 2. FIX THE ERROR: Ensure this function accepts these 3 parameters
    fun fetchTimetableData(identifier: String, isCategory: Boolean, date: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val facilities = if (isCategory) {
                    facilityRepo.getFacilitiesByCategory(identifier)
                } else {
                    facilityRepo.getFacilitiesByLocation(identifier)
                }

                _uiState.update { it.copy(facilitiesList = facilities, isLoading = false) }

                // CRITICAL: Call the listener here every time the category changes
                loadBookingsForDate(date)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    // Helper to match the format used in your saveBooking logic
    // 1. Update the Helper Function to match Firebase order
    fun formatForFirebase(dateStr: String): String {
        return try {
            // Input from picker: "2025-12-24"
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

            // MATCH THIS TO FIREBASE: "2025 / Dec / 24 (Wed)"
            val outputFormat = SimpleDateFormat("yyyy / MMM / dd (EEE)", Locale.ENGLISH)

            val date = inputFormat.parse(dateStr)
            date?.let { outputFormat.format(it) } ?: dateStr
        } catch (e: Exception) {
            dateStr
        }
    }

    // 2. Update the Init Block so it starts with the correct format
    init {
        val sdf = SimpleDateFormat("yyyy / MMM / dd (EEE)", Locale.ENGLISH)
        val today = sdf.format(Calendar.getInstance().time)
        _uiState.update { it.copy(selectedDate = today) }
    }

    // 3. Update updateDate to use the new helper
    fun updateDate(newDate: String) {
        val formattedDate = formatForFirebase(newDate) // Now returns "2025 / Dec / 24 (Wed)"
        _uiState.update { it.copy(selectedDate = formattedDate) }
        loadBookingsForDate(formattedDate)
    }


    fun loadBookingsForDate(dateString: String) {
        bookingsListener?.remove()

        _uiState.update { it.copy(isLoading = true) }

        bookingsListener = db.collection("bookings")
            .whereEqualTo("date", dateString) // dateString is now "21 / Dec / 2025 (Sun)"
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Timetable", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)

                    // Update the state with the new bookings
                    _uiState.update { it.copy(allBookings = bookings, isLoading = false) }

                    Log.d("Timetable", "Received ${bookings.size} bookings for $dateString")
                }
            }
    }

    fun listenToBookingsForDate(date: String) {
        db.collection("bookings")
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val newBookings = snapshot.toObjects(Booking::class.java)

                    // Update the UI State - THIS triggers the screen to refresh
                    _uiState.update { it.copy(allBookings = newBookings) }

                    Log.d("TT", "UI updated with ${newBookings.size} bookings")
                }
            }
    }
    private var bookingsListener: ListenerRegistration? = null


//    fun loadBookingsForDate(dateString: String) {
//        // 1. Remove/Stop the previous listener if it exists
//        bookingsListener?.remove()
//
//        // 2. Start the new real-time listener
//        bookingsListener = db.collection("bookings")
//            .whereEqualTo("date", dateString)
//            .addSnapshotListener { snapshot, error ->
//                if (error != null) {
//                    Log.e("Timetable", "Listen failed.", error)
//                    return@addSnapshotListener
//                }
//
//                if (snapshot != null) {
//                    // Convert documents to Booking objects
//                    val bookings = snapshot.toObjects(Booking::class.java)
//
//                    // 3. Update the UI state so the screen re-draws
//                    _uiState.update { it.copy(allBookings = bookings, isLoading = false) }
//
//                    Log.d("Timetable", "Received ${bookings.size} bookings for $dateString")
//                }
//            }
//    }

    fun listenToBookings(date: String) {
        db.collection("bookings")
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, _ -> // This 'listens' for changes
                val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                _uiState.update { it.copy(allBookings = bookings) }
            }
    }

    private val firestore = FirebaseFirestore.getInstance()

    fun getSlotStatus(facility: Facility, hour: Int): String {
        val selectedDate = uiState.value.selectedDate // Format: "2023-12-25"
        val currentBookings = uiState.value.allBookings

        // --- 1. PRIORITY: CLOSED (Operating Hours) ---
        // If the building isn't open, it doesn't matter if it's booked or maintained
        val startHour = facility.startTime.split(":")[0].toIntOrNull() ?: 8
        val endHour = facility.endTime.split(":")[0].toIntOrNull() ?: 22
        if (hour < startHour || hour >= endHour) return "Closed"

        // --- 2. PRIORITY: MAINTENANCE (Daily Breaks & Special Closures) ---
        // Check if the current hour is a standard break (like 1pm-2pm every day)
        if (facility.dailyBreakHours.contains(hour)) return "Maintenance"

        // Check if the current date is in the special closures map
        if (facility.specialClosures.containsKey(selectedDate)) {
            val closedHours = facility.specialClosures[selectedDate] ?: emptyList()
            if (closedHours.contains(hour)) return "Maintenance"
        }

        // --- 3. PRIORITY: BOOKED (Student Bookings) ---
        val isBooked = currentBookings.any { booking ->
            // Check both fields to ensure we don't miss the match
            val venueMatch = (booking.finalVenue.trim().equals(facility.name.trim(), ignoreCase = true)) ||
                    (booking.venue.trim().equals(facility.name.trim(), ignoreCase = true))

            val timeMatch = booking.hoursList.contains(hour)
            val isActive = booking.status != "Cancelled"

            venueMatch && timeMatch && isActive
        }

        if (isBooked) return "Booked"

        // --- 4. DEFAULT: AVAILABLE ---
        return "Available"
    }

//    fun updateDate(newDate: String) {
//        _uiState.update { it.copy(selectedDate = newDate) }
//        loadBookingsForDate(newDate)
//    }
}