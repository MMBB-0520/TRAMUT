package com.example.tramut.userInterface.check

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.ErrorRed
import com.example.tramut.ui.theme.TRAMUTTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val DarkHeader = Color(0xFF000000)

// --- 1. STATE DEFINITION ---
sealed class EntryUiState {
    object Idle : EntryUiState()
    object Loading : EntryUiState()
    object Success : EntryUiState()
    data class Error(val message: String) : EntryUiState()
}

// --- 2. VIEWMODEL (LOGIC FOR BOTH) ---
class EntryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<EntryUiState>(EntryUiState.Idle)
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    fun performCheckIn(bookingId: String, enteredId: String) {
        viewModelScope.launch {
            _uiState.value = EntryUiState.Loading
            delay(1000)
            // val booking = repository.getBooking(bookingId)
            val booking = mockGetBookingFromDb(bookingId)

            if (booking == null) {
                _uiState.value = EntryUiState.Error("Booking not found.")
                return@launch
            }

            // A. Auto-Cancellation Rule
            if (isBookingExpired(booking)) {
                _uiState.value = EntryUiState.Error("Check-in failed: Booking expired (15-min rule).")
                return@launch
            }

            // B. Authorization Rule
            if (isAuthorized(booking, enteredId)) {
                val checkInTime = getCurrentTime()

                //save to db code
                // repository.updateCheckIn(bookingId, checkInTime, status = "Checked In")
                println("Saving Check-In Time to DB: $checkInTime")
                _uiState.value = EntryUiState.Success
            } else {
                _uiState.value = EntryUiState.Error("ID $enteredId is not authorized for this booking.")
            }
        }
    }

    fun performCheckOut(bookingId: String, enteredId: String) {
        viewModelScope.launch {
            _uiState.value = EntryUiState.Loading
            delay(1000)

            //val booking = repository.getBooking(bookingId)
            val booking = mockGetBookingFromDb(bookingId)

            if (booking == null) {
                _uiState.value = EntryUiState.Error("Booking not found.")
                return@launch
            }

            // A. Authorization Rule (Only members/booker can check out)
            if (isAuthorized(booking, enteredId)) {
                val checkOutTime = getCurrentTime()

                // save to db here
                // repository.updateCheckOut(bookingId, checkOutTime, status = "Completed")
                println("Saving Check-Out Time to DB: $checkOutTime")
                _uiState.value = EntryUiState.Success
            } else {
                _uiState.value = EntryUiState.Error("ID $enteredId is not authorized to check out.")
            }
        }
    }

    // Helper: Check if User is Booker or Member
    private fun isAuthorized(booking: Booking, id: String): Boolean {
        return booking.userId == id || booking.members.any { it.first == id }
    }

    // Helper: Check 15-min expiry
    private fun isBookingExpired(booking: Booking): Boolean {
        if (booking.status.equals("Cancelled", ignoreCase = true)) return true
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val bookingDateTimeStr = "${booking.date} ${booking.startTime}"
            val startDateTime = dateFormat.parse(bookingDateTimeStr) ?: return false

            val calendar = Calendar.getInstance().apply { time = startDateTime }
            calendar.add(Calendar.MINUTE, 15) // Cutoff time

            Date().after(calendar.time) // Current time > Cutoff
        } catch (e: Exception) {
            false
        }
    }

    fun resetState() {
        _uiState.value = EntryUiState.Idle
    }

    // Mock DB
    private fun mockGetBookingFromDb(id: String): Booking {
        return Booking(
            bookingId = id,
            userId = "1234",
            bookingNo = "BK-001",
            date = "2023-12-15", // Ensure this matches testing date
            startTime = "09:00",
            duration = "2 Hours",
            building = "CIT",
            level = "1",
            checkIn = "",
            checkOut = "",
            status = "Booked",
            members = listOf(Pair("5678", "Member Name"))
        )
    }
}

// --- 3. UI IMPLEMENTATION (UNIFIED SCREEN) ---

/**
 * Reusable Screen for both Check-In and Check-Out.
 * Set [isCheckIn] to false for Check-Out mode.
 */
@Composable
fun ManualEntryScreen(
    bookingId: String,
    isCheckIn: Boolean = true, // Toggle this for Check-Out
    initialId: String = "",
    onSuccess: (String) -> Unit,
    onBackClicked: () -> Unit = {},
    viewModel: EntryViewModel = viewModel()
) {
    var studentId by remember { mutableStateOf(initialId) }
    val uiState by viewModel.uiState.collectAsState()

    val isIdLengthValid = studentId.isNotBlank() && studentId.length >= 4
    val isLoading = uiState is EntryUiState.Loading

    // Text & Strings based on Mode
    val screenTitle = if (isCheckIn) "Check-In" else "Check-Out"
    val successMessage = if (isCheckIn) "Check-In Successful" else "Check-Out Successful"

    // Handle Success Navigation
    LaunchedEffect(uiState) {
        if (uiState is EntryUiState.Success) {
            onSuccess(successMessage)
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = { CheckInTopBar(title = screenTitle, onBackClicked = onBackClicked) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = "Student/Staff ID",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            OutlinedTextField(
                value = studentId,
                onValueChange = {
                    if (it.length <= 10) {
                        studentId = it.filter { char -> char.isDigit() }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DarkHeader,
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    if (isIdLengthValid) {
                        if (isCheckIn) {
                            viewModel.performCheckIn(bookingId, studentId)
                        } else {
                            viewModel.performCheckOut(bookingId, studentId)
                        }
                    }
                },
                enabled = isIdLengthValid && !isLoading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkHeader)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Verifying...", color = Color.White)
                } else {
                    Text(text = "Confirmation", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Error Dialog
        if (uiState is EntryUiState.Error) {
            val errorMessage = (uiState as EntryUiState.Error).message
            AlertDialog(
                onDismissRequest = { viewModel.resetState() },
                title = { Text(text = "Action Failed", fontWeight = FontWeight.Bold) },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.resetState() },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("OK", color = Color.White)
                    }
                },
                containerColor = Background,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

// --- 4. HELPER COMPONENTS ---

@Composable
fun CheckInTopBar(title: String, onBackClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkHeader)
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = Background, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBackClicked) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(start = 8.dp)

                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Mode: Check-In")
@Composable
fun PreviewManualCheckIn() {
    TRAMUTTheme {
        ManualEntryScreen(
            bookingId = "dummy",
            isCheckIn = true,
            initialId = "1234",
            onSuccess = {},
            onBackClicked = {}
        )
    }
}

@Preview(showBackground = true, name = "Mode: Check-Out")
@Composable
fun PreviewManualCheckOut() {
    TRAMUTTheme {
        ManualEntryScreen(
            bookingId = "dummy",
            isCheckIn = false,
            initialId = "1234",
            onSuccess = {},
            onBackClicked = {}
        )
    }
}