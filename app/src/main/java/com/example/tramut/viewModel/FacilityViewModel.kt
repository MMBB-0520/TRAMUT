package com.example.myfacilitybookingsystem.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.rooms.entity.Booking // Make sure this is imported
import com.example.myfacilitybookingsystem.rooms.repo.FacilityRepository
import kotlinx.coroutines.launch

class FacilityViewModel : ViewModel() {

    private val repository = FacilityRepository()

    val facilityList = mutableStateListOf<Facility>()
    val isLoading = mutableStateOf(false)

    // Form States
    var formName = mutableStateOf("")
    var formCapacity = mutableStateOf("")
    var formDesc = mutableStateOf("")
    var formStatus = mutableStateOf("Available")
    var formStartTime = mutableStateOf("08:00")
    var formEndTime = mutableStateOf("22:00")

    // UI Feedback
    var operationStatus = mutableStateOf<String?>(null)

    // 1. LOAD FACILITIES
    fun loadFacilities(department: String) {
        viewModelScope.launch {
            isLoading.value = true
            repository.getFacilitiesFlow(department).collect { list ->
                facilityList.clear()
                facilityList.addAll(list)
                isLoading.value = false
            }
        }
    }

    // 2. PREPARE FORM FOR EDITING
    fun selectFacilityForEdit(facility: Facility) {
        formName.value = facility.name
        formCapacity.value = if (facility.capacity > 0) facility.capacity.toString() else ""
        formStatus.value = facility.status
        formStartTime.value = facility.startTime
        formEndTime.value = facility.endTime
        // Note: If you need to edit 'specialClosures', you need state variables for that too
    }

    // 3. RESET FORM
    fun clearForm() {
        formName.value = ""
        formCapacity.value = ""
        formDesc.value = ""
        formStatus.value = "Available"
        formStartTime.value = "08:00"
        formEndTime.value = "22:00"
        operationStatus.value = null
    }

    // 4. ADD FACILITY
    fun addFacility(
        department: String,
        specialClosures: Map<String, List<Int>>?, // Made nullable for safety
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val newFac = Facility(
                name = formName.value,
                department = department,
                capacity = formCapacity.value.toIntOrNull() ?: 0,
                status = "Available",
                startTime = formStartTime.value,
                endTime = formEndTime.value,
                // Ensure your Facility entity has this field:
                specialClosures = specialClosures ?: emptyMap()
            )

            val result = repository.addFacility(newFac)
            if (result.isSuccess) onSuccess() else operationStatus.value = "Error adding facility"
        }
    }

    // 5. UPDATE FACILITY
    fun updateFacility(
        docId: String,
        department: String,
        specialClosures: Map<String, List<Int>>?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val updatedFac = Facility(
                id = docId,
                name = formName.value,
                department = department,
                capacity = formCapacity.value.toIntOrNull() ?: 0,
                status = formStatus.value,
                startTime = formStartTime.value,
                endTime = formEndTime.value,
                specialClosures = specialClosures ?: emptyMap()
            )

            val result = repository.updateFacility(updatedFac)
            if (result.isSuccess) onSuccess() else operationStatus.value = "Error updating facility"
        }
    }

    // 6. SAVE BOOKING (Corrected)
    // We launch a coroutine here and call the repository
    fun saveBooking(booking: Booking, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            val success = repository.saveBooking(booking)
            isLoading.value = false
            onResult(success)
        }
    }

    // 7. DELETE FACILITY
    fun deleteFacility(docId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteFacility(docId)
            if (result.isSuccess) onSuccess() else operationStatus.value = "Error deleting facility"
        }
    }
}