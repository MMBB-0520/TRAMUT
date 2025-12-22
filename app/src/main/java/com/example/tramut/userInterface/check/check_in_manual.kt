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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

val DarkHeader = Color(0xFF000000)

sealed class EntryUiState {
    object Idle : EntryUiState()
    object Loading : EntryUiState()
    object Success : EntryUiState()
    data class Error(val message: String) : EntryUiState()
}

class EntryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val bookingsCollection = db.collection("bookings")

    private val _uiState = MutableStateFlow<EntryUiState>(EntryUiState.Idle)
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    // Helper: Fetch Booking by explicit ID
    private suspend fun getBookingFromDb(id: String): Booking? {
        return try {
            val doc = bookingsCollection.document(id).get().await()
            if (doc.exists()) {
                doc.toObject(Booking::class.java)?.copy(bookingId = doc.id)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper: Find Active Booking for a User (Auto-Discovery)
    private suspend fun findActiveBookingForUser(userId: String): Booking? {
        // 1. FIX: Match the exact format in your Firestore image
        // Pattern: "dd / MMM / yyyy (EEE)" -> "17 / Dec / 2025 (Wed)"
        val databaseDateFormat = SimpleDateFormat("yyyy / MMM / dd (EEE)", Locale.US)
        val todayDate = databaseDateFormat.format(Date())

        return try {
            // 2. FIX: Query ONLY by Date first.
            // We cannot query by "userId" because that misses the "members" array.
            val querySnapshot = bookingsCollection
                .whereEqualTo("date", todayDate)
                .get()
                .await()

            val todayBookings = querySnapshot.toObjects(Booking::class.java)

            // 3. Filter in Memory
            // Find a booking where the scanned ID matches the Owner OR a Member
            todayBookings.firstOrNull { booking ->
                val isOwner = booking.userId == userId

                // Check if the ID exists inside the members list (assuming List<Pair<String, String>> or similar)
                // Adjust ".first" depending on your Member data class structure
                val isMember = booking.members.any { it.id == userId }

                val isParticipant = isOwner || isMember

                // Check status (ignore Cancelled/Completed)
                val isValidStatus = booking.status.equals("Checked In", ignoreCase = true) ||
                        booking.status.equals("Booked", ignoreCase = true)

                isParticipant && isValidStatus
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    // --- SHARED RESOLVER LOGIC ---
    private suspend fun resolveBooking(bookingId: String, enteredId: String): Booking? {
        // Scenario A: explicit ID from scanner
        if (bookingId.isNotBlank() && bookingId != "no_id" && bookingId != "admin_override") {
            return getBookingFromDb(bookingId)
        }
        // Scenario B: search by User ID
        return findActiveBookingForUser(enteredId)
    }

    fun performCheckIn(bookingId: String, enteredId: String) {
        viewModelScope.launch {
            _uiState.value = EntryUiState.Loading
            delay(500)

            val booking = resolveBooking(bookingId, enteredId)

            if (booking == null) {
                _uiState.value = EntryUiState.Error("No active booking found for ID: $enteredId today.")
                return@launch
            }

            if (isTooEarly(booking)) {
                _uiState.value = EntryUiState.Error("Too early! Check-in starts at ${booking.startTime}.")
                return@launch
            }

            // A. Auto-Cancellation Rule
            if (isBookingExpired(booking)) {
                _uiState.value = EntryUiState.Error("Check-in failed: Booking expired.")
                return@launch
            }

            // B. Authorization Rule
            if (isAuthorized(booking, enteredId)) {
                val currentTime = getCurrentTime()

                try {
                    bookingsCollection.document(booking.bookingId).update(
                        mapOf(
                            "status" to "Checked In",
                            "checkIn" to currentTime,
                            "bookingStatus" to "Checked In"
                        )
                    ).await()

                    _uiState.value = EntryUiState.Success
                } catch (e: Exception) {
                    _uiState.value = EntryUiState.Error("Failed to update database: ${e.message}")
                }
            } else {
                _uiState.value = EntryUiState.Error("ID $enteredId is not authorized.")
            }
        }
    }

    fun performCheckOut(bookingId: String, enteredId: String) {
        viewModelScope.launch {
            _uiState.value = EntryUiState.Loading
            delay(500)

            val booking = resolveBooking(bookingId, enteredId)

            if (booking == null) {
                _uiState.value = EntryUiState.Error("No active booking found for ID: $enteredId")
                return@launch
            }

            if (booking.status.equals("Completed", ignoreCase = true)) {
                _uiState.value = EntryUiState.Error("Room is already checked out.")
                return@launch
            }

            // --- NEW CODE STARTS HERE ---
            // Validate Checkout Time Limit
            if (isCheckoutLate(booking)) {
                _uiState.value = EntryUiState.Error("Check-out failed: Session expired.")
                // Optional: You might want to auto-complete it in the background instead of showing an error
                return@launch
            }
            // --- NEW CODE ENDS HERE ---

            if (isAuthorized(booking, enteredId)) {
                val currentTime = getCurrentTime()

                try {
                    bookingsCollection.document(booking.bookingId).update(
                        mapOf(
                            "status" to "Completed",
                            "checkOut" to currentTime,
                            "bookingStatus" to "Completed"
                        )
                    ).await()

                    _uiState.value = EntryUiState.Success
                } catch (e: Exception) {
                    _uiState.value = EntryUiState.Error("Database error: ${e.message}")
                }
            } else {
                _uiState.value = EntryUiState.Error("ID $enteredId is not authorized to check out this room.")
            }
        }
    }

    // Helper: Check if User is Booker or Member
    private fun isAuthorized(booking: Booking, id: String): Boolean {
        // Matches Booker OR Matches any member in the list
        return booking.userId == id || booking.members.any { it.id == id }
    }

    // Helper: Check if it is too early to check in
    private fun isTooEarly(booking: Booking): Boolean {
        return try {
            // Use the format that matches your database
            val dbDateFormat = SimpleDateFormat("yyyy / MMM / dd (EEE) h:mm a", Locale.US)

            // Combine Date + StartTime (e.g. "17 / Dec / 2025 (Wed) 1:00 PM")
            val bookingStartStr = "${booking.date} ${booking.startTime}"
            val startDateTime = dbDateFormat.parse(bookingStartStr) ?: return false

            // Optional: Allow check-in 5 minutes early?
            // If so, subtract 5 minutes from startDateTime before comparing:
            // val calendar = Calendar.getInstance().apply { time = startDateTime }
            // calendar.add(Calendar.MINUTE, -5)
            // return Date().before(calendar.time)

            // Strict Rule: Must be exactly start time or later
            Date().before(startDateTime)
        } catch (e: Exception) {
            false // Fail open or closed depending on preference
        }
    }

    // Helper: Check 15-min expiry
    private fun isBookingExpired(booking: Booking): Boolean {
        if (booking.status.equals("Cancelled", ignoreCase = true)) return true
        if (booking.status.equals("Completed", ignoreCase = true)) return true

        return try {
            // MATCH THE DATABASE FORMAT HERE TOO
            val dbDateFormat = SimpleDateFormat("yyyy / MMM / dd (EEE) h:mm a", Locale.US)

            // Combine DB date + DB start time (e.g. "17 / Dec / 2025 (Wed) 1:00 PM")
            val bookingDateTimeStr = "${booking.date} ${booking.startTime}"
            val startDateTime = dbDateFormat.parse(bookingDateTimeStr) ?: return false

            val calendar = Calendar.getInstance().apply { time = startDateTime }
            calendar.add(Calendar.MINUTE, 15) // 15 minute grace period

            Date().after(calendar.time)
        } catch (e: Exception) {
            false
        }
    }

    private fun isCheckoutLate(booking: Booking): Boolean {
        return try {
            // Use the same date format as the rest of your app
            val dbDateFormat = SimpleDateFormat("yyyy / MMM / dd (EEE) h:mm a", Locale.US)

            // 1. Construct the full End Date/Time string
            // NOTE: Ensure your Booking entity has an 'endTime' field.
            // If not, you must calculate it: (startTime + duration)
            val bookingEndStr = "${booking.date} ${booking.endTime}"

            val endDateTime = dbDateFormat.parse(bookingEndStr) ?: return false

            // 2. Add a Grace Period (e.g., 15 minutes to pack up)
            val calendar = Calendar.getInstance().apply { time = endDateTime }
            calendar.add(Calendar.MINUTE, 15)

            // 3. Return true if current time is AFTER the allowed window
            Date().after(calendar.time)
        } catch (e: Exception) {
            false // If parsing fails, we default to allowing it (or return true to block)
        }
    }

    fun resetState() {
        _uiState.value = EntryUiState.Idle
    }
}

// check-in manual entry screen
@Composable
fun ManualEntryScreen(
    bookingId: String,
    isCheckIn: Boolean = true,
    initialId: String = "",
    onSuccess: (String) -> Unit,
    onBackClicked: () -> Unit = {},
    viewModel: EntryViewModel = viewModel()
) {
    var studentId by remember { mutableStateOf(initialId) }
    val uiState by viewModel.uiState.collectAsState()

    // Validation: simple length check
    val isIdLengthValid = studentId.isNotBlank() && studentId.length >= 4 && studentId.length <= 7
    val isLoading = uiState is EntryUiState.Loading

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
                    if (it.length <= 15) { // Limit length
                        studentId = it
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), // Changed to Text to allow alphanumeric IDs if needed
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

//topbar for checkin and check out
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
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

