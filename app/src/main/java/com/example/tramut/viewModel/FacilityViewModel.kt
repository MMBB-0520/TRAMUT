package com.example.myfacilitybookingsystem.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.rooms.repo.FacilityRepository
import kotlinx.coroutines.launch

class FacilityViewModel : ViewModel() {

    private val repository = FacilityRepository()

    val facilityList = mutableStateListOf<Facility>()
    val isLoading = mutableStateOf(false)

    // State for Add/Edit Form
    var formName = mutableStateOf("")
    var formType = mutableStateOf("")
    var formCapacity = mutableStateOf("")
    var formDesc = mutableStateOf("")
    var formStatus = mutableStateOf("Available")

    // Status Message
    var operationStatus = mutableStateOf<String?>(null)

    // Load Data
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

    fun selectFacilityForEdit(facility: Facility) {
        formName.value = facility.name
        formType.value = facility.type
        formCapacity.value = facility.capacity.toString()
        formDesc.value = facility.description
        formStatus.value = facility.status
    }

    fun clearForm() {
        formName.value = ""
        formType.value = ""
        formCapacity.value = ""
        formDesc.value = ""
        formStatus.value = "Available"
        operationStatus.value = null
    }

    fun addFacility(department: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val newFac = Facility(
                name = formName.value,
                type = formType.value,
                department = department,
                capacity = formCapacity.value.toIntOrNull() ?: 0,
                description = formDesc.value,
                status = formStatus.value
            )
            val result = repository.addFacility(newFac)
            if (result.isSuccess) onSuccess() else operationStatus.value = "Error adding facility"
        }
    }

    fun updateFacility(docId: String, department: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val updatedFac = Facility(
                id = docId,
                name = formName.value,
                type = formType.value,
                department = department,
                capacity = formCapacity.value.toIntOrNull() ?: 0,
                description = formDesc.value,
                status = formStatus.value
            )
            val result = repository.updateFacility(updatedFac)
            if (result.isSuccess) onSuccess() else operationStatus.value = "Error updating facility"
        }
    }

    fun deleteFacility(docId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = repository.deleteFacility(docId)
            if (result.isSuccess) onSuccess() else operationStatus.value = "Error deleting facility"
        }
    }
}