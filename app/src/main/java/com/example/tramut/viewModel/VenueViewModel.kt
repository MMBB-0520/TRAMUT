package com.example.tramut.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.rooms.repo.TimetableRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VenueUiState(
    val venues: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class VenueViewModel(
    private val repository: TimetableRepository = TimetableRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(VenueUiState())
    val uiState = _uiState.asStateFlow()

    fun loadVenues(department: String) {
        viewModelScope.launch {
            _uiState.value = VenueUiState(isLoading = true)

            try {
                val facilities = repository.getFacilitiesByDepartment(department)
                val venueNames = facilities.map { it.name }.distinct()

                _uiState.value = VenueUiState(
                    venues = venueNames,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = VenueUiState(
                    venues = emptyList(),
                    isLoading = false,
                    error = e.message ?: "Failed to load venues"
                )
            }
        }
    }

    fun clearVenues() {
        _uiState.value = VenueUiState()
    }
}