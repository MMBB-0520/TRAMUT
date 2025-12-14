package com.example.myfacilitybookingsystem.viewModel
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.example.myfacilitybookingsystem.rooms.repo.AnnouncementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

data class AnnouncementUiState(
    val title: String = "",
    val description: String = "",
    val venueType: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

class AnnouncementViewModel : ViewModel() {

    private val repository = AnnouncementRepository()
    private val _uiState = MutableStateFlow(AnnouncementUiState())
    val uiState: StateFlow<AnnouncementUiState> = _uiState.asStateFlow()

    // Load data for editing
    fun loadAnnouncement(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val data = repository.getAnnouncementById(id)
            if (data != null) {
                _uiState.update {
                    it.copy(
                        title = data.title,
                        description = data.content,
                        venueType = data.venue,
                        startDate = data.created_date_str,
                        endDate = data.expiry_date_str,
                        isLoading = false
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Not Found") }
            }
        }
    }

    // Inputs
    fun onTitleChange(v: String) = _uiState.update { it.copy(title = v) }
    fun onDescriptionChange(v: String) = _uiState.update { it.copy(description = v) }
    fun onVenueChange(v: String) = _uiState.update { it.copy(venueType = v) }
    fun onStartDateChange(v: String) = _uiState.update { it.copy(startDate = v) }
    fun onEndDateChange(v: String) = _uiState.update { it.copy(endDate = v) }



    fun resetState() { _uiState.value = AnnouncementUiState() }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveAnnouncement(docId: String? = null, department: String, adminId: String, onSuccess: () -> Unit) {
        val s = _uiState.value

        // 1. VALIDATION CHECKS
        if (s.title.isBlank() || s.description.isBlank()) {
            _uiState.update { it.copy(error = "Title and Description cannot be empty.") }
            return
        }

        // 2. DATE VALIDATION (No Past Dates)
        if (isDateInPast(s.startDate)) {
            _uiState.update { it.copy(error = "Start date cannot be in the past.") }
            return
        }

        if (isDateInPast(s.endDate)) {
            _uiState.update { it.copy(error = "End date cannot be in the past.") }
            return
        }

        // Check if End Date is before Start Date
        if (isEndDateBeforeStartDate(s.startDate, s.endDate)) {
            _uiState.update { it.copy(error = "End date cannot be before Start date.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) } // Clear previous errors

            // ... (Rest of your existing save logic) ...

            val result = if (docId != null) {
                repository.updateAnnouncement(docId, s.title, s.description, s.venueType, s.startDate, s.endDate)
            } else {
                val newAn = Announcement(
                    title = s.title,
                    content = s.description,
                    department = department,
                    admin_id = adminId,
                    venue = s.venueType,
                    created_date_str = s.startDate,
                    expiry_date_str = s.endDate
                )
                repository.addAnnouncement(newAn)
            }

            // ... (Rest of your logic) ...
        }
    }

    // --- HELPER FUNCTIONS ---

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isDateInPast(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false // Skip check if empty (or force validation if required)
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val inputDate = LocalDate.parse(dateStr, formatter)
            val today = LocalDate.now()
            inputDate.isBefore(today) // Returns true if date is in the past
        } catch (e: DateTimeParseException) {
            true // Treat invalid format as an error (or handle separately)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isEndDateBeforeStartDate(startStr: String, endStr: String): Boolean {
        if (startStr.isBlank() || endStr.isBlank()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val start = LocalDate.parse(startStr, formatter)
            val end = LocalDate.parse(endStr, formatter)
            end.isBefore(start)
        } catch (e: Exception) {
            true
        }
    }
}
