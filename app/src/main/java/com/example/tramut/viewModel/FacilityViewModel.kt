package com.example.myfacilitybookingsystem.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.entity.Booking
import com.example.myfacilitybookingsystem.rooms.repo.FacilityRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch

class FacilityViewModel : ViewModel() {

    private val repository = FacilityRepository()

    val facilityList = mutableStateListOf<Facility>()
    val isLoading = mutableStateOf(false)

    // --- FORM STATES ---
    var formCategory = mutableStateOf("")
    var formName = mutableStateOf("")
    var formCapacity = mutableStateOf("")
    var formDesc = mutableStateOf("")
    var formStatus = mutableStateOf("Available")
    var formStartTime = mutableStateOf("08:00")
    var formEndTime = mutableStateOf("22:00")

    // Error/Success Message for UI
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

    // 2. PREPARE FORM FOR EDITING (CRASH PROOFED)
    fun selectFacilityForEdit(facility: Facility?) {
        if (facility == null) return

        formName.value = facility.name
        formCategory.value = facility.roomCode // Ensure category is loaded
        formCapacity.value = if (facility.capacity > 0) facility.capacity.toString() else ""
        formStatus.value = facility.status
        formStartTime.value = facility.startTime
        formEndTime.value = facility.endTime
    }

    // 3. RESET FORM
    fun clearForm() {
        formName.value = ""
        formCategory.value = ""
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
        specialClosures: Map<String, List<Int>>,
        onResult: (Boolean, String?) -> Unit
    ) {
        val facilityData = hashMapOf(
            "name" to formName.value,
            "category" to formCategory.value,
            "department" to department,
            "startTime" to formStartTime.value,
            "endTime" to formEndTime.value,
            "status" to "Available",
            "capacity" to (formCapacity.value.toIntOrNull() ?: 1),
            "specialClosures" to specialClosures
        )

        Firebase.firestore.collection("facilities")
            .add(facilityData)
            .addOnSuccessListener {
                clearForm()
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Unknown Error")
            }
    }

    // 5. UPDATE FACILITY (FIXED - NO _uiState)
    fun updateFacility(
        docId: String,
        department: String,
        specialClosures: Map<String, List<Int>>?,
        onResult: (Boolean, String?) -> Unit // Changed to Callback
    ) {
        val updatedData = mapOf(
            "name" to formName.value,
            "category" to formCategory.value,
            "department" to department,
            "capacity" to (formCapacity.value.toIntOrNull() ?: 0),
            "status" to formStatus.value,
            "startTime" to formStartTime.value,
            "endTime" to formEndTime.value,
            "specialClosures" to (specialClosures ?: emptyMap())
        )

        Firebase.firestore.collection("facilities").document(docId)
            .update(updatedData)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    // 6. DELETE FACILITY
    fun deleteFacility(docId: String, onSuccess: () -> Unit) {
        Firebase.firestore.collection("facilities").document(docId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { operationStatus.value = "Delete failed" }
    }
}