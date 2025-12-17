package com.example.myfacilitybookingsystem.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.TimetableRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private var bookingsListener: ListenerRegistration? = null

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
                    val capacityData = doc.get("capacity")
                    val capacityList: List<Long> = when (capacityData) {
                        is List<*> -> capacityData.filterIsInstance<Long>()
                        is Long -> listOf(capacityData)
                        else -> emptyList()
                    }

                    val breakHoursData = doc.get("dailyBreakHours")
                    val breakHoursList: List<Int> = when (breakHoursData) {
                        is List<*> -> breakHoursData.mapNotNull {
                            (it as? Long)?.toInt() ?: (it as? Int)
                        }

                        is String -> breakHoursData.split(",")
                            .mapNotNull { it.trim().toIntOrNull() }

                        else -> emptyList()
                    }

                    val rawSpecialClosures =
                        doc.get("specialClosures") as? Map<String, Any> ?: emptyMap()
                    val convertedSpecialClosures = rawSpecialClosures.mapValues { entry ->
                        val hoursList = entry.value as? List<*>
                        hoursList?.mapNotNull {
                            when (it) {
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
                        specialClosures = convertedSpecialClosures
                    )
                }

                _uiState.update { it.copy(facilitiesList = liveList, isLoading = false) }
                // Start listening for bookings for the specific date
                loadBookingsForDate(date)
            }
        }
    }

    fun loadBookingsForDate(date: String) {
        bookingsListener?.remove()

        bookingsListener = db.collection("bookings")
            .whereEqualTo("date", date)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TimetableViewModel", "Booking listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val liveBookings = snapshot.toObjects(Booking::class.java)
                    _uiState.update { it.copy(bookingsList = liveBookings) }
                    Log.d("TimetableViewModel", "Real-time update: ${liveBookings.size} bookings")
                }
            }
    }

    fun autoAssignFacilityId(category: String, date: String, hour: Int): String? {
        // 1. Access your current list of facilities from the UI state
        val facilities = uiState.value.facilitiesList

        // 2. Loop through all facilities currently showing (e.g., all Pickleball courts)
        for (facility in facilities) {
            // 3. Check the status of this specific facility for this specific hour
            val status = getSlotStatus(facility, hour)

            // 4. Return the first ID that is green/available
            if (status == "Available") {
                return facility.id
            }
        }

        return null
    }

    fun updateDate(newDate: String) {
        _uiState.update { it.copy(selectedDate = newDate) }
        loadBookingsForDate(newDate)
    }

    fun getSlotStatus(facility: Facility, hour: Int): String {
        val dateString = uiState.value.selectedDate

        val facilityStartHour = facility.startTime.split(":")[0].toIntOrNull() ?: 8
        val facilityEndHour = facility.endTime.split(":")[0].toIntOrNull() ?: 22

        if (hour < facilityStartHour || hour >= facilityEndHour) return "Closed"
        if (facility.dailyBreakHours.contains(hour)) return "Maintenance"

        val specialClosedHours = facility.specialClosures[dateString]
        if (specialClosedHours != null && specialClosedHours.contains(hour)) {
            return "Maintenance"
        }

        val isBooked = uiState.value.bookingsList.any { booking ->
            val bookedStartHour = booking.startTime.split(":")[0].toIntOrNull() ?: 0
            val bookedEndHour = booking.endTime.split(":")[0].toIntOrNull() ?: 0

            val venueMatches = booking.venue.trim().equals(facility.name.trim(), ignoreCase = true)

            venueMatches &&
                    booking.date == dateString &&
                    hour >= bookedStartHour &&
                    hour < bookedEndHour
        }

        return if (isBooked) "Booked" else "Available"
    }
}