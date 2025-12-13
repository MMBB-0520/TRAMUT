package com.example.myfacilitybookingsystem.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.example.myfacilitybookingsystem.rooms.repo.AnnouncementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    // Save (Create or Update)
    fun saveAnnouncement(docId: String? = null, department: String, adminId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val s = _uiState.value

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

            _uiState.update { it.copy(isSaving = false) }
            if (result.isSuccess) {
                if (docId == null) resetState()
                onSuccess()
            } else {
                _uiState.update { it.copy(error = "Operation failed") }
            }
        }
    }

    fun resetState() { _uiState.value = AnnouncementUiState() }
}