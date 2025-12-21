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
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Fetch the list of facilities (The Venue names on the left of the chart)
                val facilities = if (isCategory) {
                    facilityRepo.getFacilitiesByCategory(identifier)
                } else {
                    facilityRepo.getFacilitiesByLocation(identifier)
                }

                _uiState.value = _uiState.value.copy(
                    facilitiesList = facilities,
                    isLoading = false
                )

                // Start listening for real-time bookings on this date
                listenToBookingsForDate(date)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message)
            }
        }
    }

    fun listenToBookingsForDate(date: String) {
        Log.d("TIMETABLE_DEBUG", "Listening for: $date")
        firestore.collection("bookings")
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, _ ->
                val bookings = snapshot?.toObjects(Booking::class.java) ?: emptyList()
                Log.d("TIMETABLE_DEBUG", "Found ${bookings.size} bookings")
                _allBookings.value = bookings
            }
    }
    private var bookingsListener: ListenerRegistration? = null


    fun autoAssignFacilityId(category: String, date: String, hour: Int): String? {
        val currentFacilities = uiState.value.facilitiesList.filter {
            it.category.equals(category, ignoreCase = true)
        }
        val currentBookings = uiState.value.allBookings

        val availableFacility = currentFacilities.find { facility ->
            val isOccupied = currentBookings.any { b ->
                // ADD .trim() to prevent spacing issues
                b.finalVenue.trim().equals(facility.name.trim(), ignoreCase = true) &&
                        b.date == date &&
                        hour in b.hoursList &&
                        b.status != "Cancelled"
            }
            !isOccupied
        }
        return availableFacility?.id
    }

    // Updates allBookings in real-time
    fun loadBookingsForDate(dateString: String) {
        db.collection("bookings")
            .whereEqualTo("date", dateString)
            .addSnapshotListener { snapshot, _ -> // <--- addSnapshotListener is the key!
                if (snapshot != null) {
                    val bookings = snapshot.toObjects(Booking::class.java)
                    _uiState.update { it.copy(allBookings = bookings) }
                }
            }
    }
    // Inside TimetableViewModel
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
        // 1. Get the current list of bookings for THIS date
        val currentBookings = _allBookings.value

        // 2. Look for a match
        val isBooked = currentBookings.any { booking ->
            // Use .trim() and .lowercase() to prevent "Badminton " matching "Badminton" failure
            val venueMatch = booking.finalVenue.trim().equals(facility.name.trim(), ignoreCase = true)

            // Ensure the hour (Int) is actually in the hoursList
            val timeMatch = booking.hoursList.contains(hour)

            venueMatch && timeMatch
        }

        return if (isBooked) "Booked" else "Available"
    }

    fun updateDate(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        loadBookingsForDate(newDate)
    }
}