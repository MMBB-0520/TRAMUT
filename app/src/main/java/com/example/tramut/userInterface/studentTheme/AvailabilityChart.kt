package com.example.tramut.userInterface.studentTheme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.example.tramut.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import com.google.firebase.auth.FirebaseAuth

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityChartScreen(
    selectedFacilityFromPrevious: String = "",
    onBookNow: (String, String) -> Unit = { _, _ -> }
) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // Date options (today + 2 days)
    val formatter = DateTimeFormatter.ofPattern("dd / MMM / yyyy (EEE)", Locale.ENGLISH)
    val today = LocalDate.now()
    val dateOptions = (0..2).map { today.plusDays(it.toLong()).format(formatter) }
    var selectedDate by remember { mutableStateOf(dateOptions[0]) }

    // Venue options
    val venueOptions = listOf(
        "Cyber Centre Discussion Room",
        "Library Discussion Room",
        "Sports Facilities"
    )
    var selectedVenue by remember { mutableStateOf(venueOptions[0]) }

    // Dialog states
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    var failureMessage by remember { mutableStateOf("") }

    // Store booked slots state
    var bookedSlots by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    // Load existing bookings when date or venue changes
    LaunchedEffect(selectedDate, selectedVenue) {
        loadBookedSlots(db, selectedDate, selectedVenue) { slots ->
            bookedSlots = slots
        }
    }

    // Scroll
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Column(
        Modifier
            .padding(18.dp)
            .fillMaxSize()
    ) {
        // Topbar
        Text(
            text = "Availability Chart",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Book now
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.LightGray.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Book Now",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Date",
                    fontSize = 14.sp
                )
                UnderlinedFloatingLabelDropdown(
                    label = "Select Date",
                    value = selectedDate,
                    items = dateOptions
                ) { selectedDate = it }
            }
        }

        // Venue Type
        Text(
            text = "Venue Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        UnderlinedFloatingLabelDropdown(
            label = "Select Venue Type",
            value = selectedVenue,
            items = venueOptions
        ) { selectedVenue = it }

        Spacer(modifier = Modifier.height(16.dp))

        // Timetable
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .border(1.dp, Color.Gray)
                .clip(RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(verticalScrollState)
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        // first column for venue
                        Box(
                            modifier = Modifier
                                .width(180.dp)
                                .height(50.dp)
                                .border(0.5.dp, Color.Gray)
                                .padding(8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "Venue/Time",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Time slot headers - horizontally scrollable
                        (11..15).forEach { hour ->
                            Box(
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(50.dp)
                                    .border(0.5.dp, Color.Gray)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${if (hour > 12) hour - 12 else hour}:00 ${if (hour >= 12) "PM" else "AM"}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Timetable Rows - S.P.Foyer Slots
                    (5..11).forEach { slotNumber ->
                        val venueName = "S.P.Foyer- Slot $slotNumber."

                        Row(
                            modifier = Modifier
                                .background(
                                    if (slotNumber % 2 == 0) Color.White
                                    else Color.LightGray.copy(alpha = 0.1f)
                                )
                        ) {
                            // Venue name cell
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(60.dp)
                                    .border(0.5.dp, Color.Gray)
                                    .padding(8.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = venueName,
                                    fontSize = 14.sp
                                )
                            }
                            // Time slot cells
                            (11..15).forEach { hour ->
                                val slotHour = hour + 1
                                val displayHour = if (slotHour > 12) slotHour - 12 else slotHour
                                val amPm = if (slotHour >= 12) "PM" else "AM"

                                // Create unique key for this slot
                                val slotKey = "${selectedDate}_${venueName}_${hour}:00"
                                val isBooked = bookedSlots[slotKey] ?: false

                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(60.dp)
                                        .border(0.5.dp, Color.Gray)
                                        .padding(8.dp)
                                        .clickable(
                                            enabled = !isBooked && currentUser != null
                                        ) {
                                            if (currentUser != null) {
                                                bookTimeSlot(
                                                    db = db,
                                                    userId = currentUser.uid,
                                                    userName = currentUser.email ?: "Anonymous",
                                                    date = selectedDate,
                                                    venue = venueName,
                                                    timeSlot = "${hour}:00",
                                                    venueType = selectedVenue,
                                                    onSuccess = {
                                                        showSuccessDialog = true
                                                        // Refresh booked slots
                                                        loadBookedSlots(
                                                            db,
                                                            selectedDate,
                                                            selectedVenue
                                                        ) { slots ->
                                                            bookedSlots = slots
                                                        }
                                                    },
                                                    onFailure = { error ->
                                                        failureMessage = error
                                                        showFailureDialog = true
                                                    }
                                                )
                                            } else {
                                                failureMessage = "Please login to book a slot"
                                                showFailureDialog = true
                                            }
                                        }
                                        .background(
                                            if (isBooked) Color.Red.copy(alpha = 0.1f)
                                            else if (currentUser == null) Color.Gray.copy(alpha = 0.1f)
                                            else Color.White
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBooked) {
                                        Text(
                                            text = "Booked",
                                            fontSize = 12.sp,
                                            color = Color.Red,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else if (slotNumber == 5) {
                                        // Show time for reference in first row
                                        Text(
                                            text = "$displayHour:00 $amPm",
                                            fontSize = 13.sp,
                                            color = Color.Gray
                                        )
                                    } else {
                                        Text(
                                            text = if (currentUser != null) "Available" else "Login to Book",
                                            fontSize = 12.sp,
                                            color = if (currentUser != null) Color.Green else Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray
        )

        // Other Sports Facilities List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .height(200.dp)
        ) {
            val otherFacilities = listOf(
                "Snooker Table 1",
                "Squash Court 1",
                "Squash Court 2",
                "Swimming Pool - Slot 1",
                "Swimming Pool - Slot 2",
                "Swimming Pool - Slot 3",
                "Swimming Pool - Slot 4",
                "Swimming Pool - Slot 5"
            )

            otherFacilities.forEach { facility ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedVenue = facility
                            onBookNow(facility, selectedDate)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = facility,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        fontSize = 14.sp
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // Success dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("OK", color = Color.White, fontSize = 24.sp)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.correct),
                            contentDescription = "SuccessBook",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Booking Successfully",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(8.dp))
        )
    }

    // Failure dialog
    if (showFailureDialog) {
        AlertDialog(
            onDismissRequest = { showFailureDialog = false },
            confirmButton = {
                Button(
                    onClick = { showFailureDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Text("OK", color = Color.White, fontSize = 24.sp)
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color(0xFFE57373), shape = RoundedCornerShape(50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.wrong),
                            contentDescription = "Booking Failed",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = failureMessage,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        softWrap = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            },
            modifier = Modifier.background(Color.White, shape = RoundedCornerShape(8.dp))
        )
    }
}

// Function to book a time slot and save to Firebase
fun bookTimeSlot(
    db: FirebaseFirestore,
    userId: String,
    userName: String,
    date: String,
    venue: String,
    timeSlot: String,
    venueType: String,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val bookingId = UUID.randomUUID().toString()
    val timestamp = System.currentTimeMillis()

    val bookingData = hashMapOf(
        "bookingId" to bookingId,
        "userId" to userId,
        "userName" to userName,
        "date" to date,
        "venue" to venue,
        "timeSlot" to timeSlot,
        "venueType" to venueType,
        "status" to "Confirmed",
        "createdAt" to timestamp,
        "slotKey" to "${date}_${venue}_${timeSlot}" // Unique identifier for this slot
    )

    // Check if slot is already booked
    db.collection("timetableBookings")
        .whereEqualTo("slotKey", "${date}_${venue}_${timeSlot}")
        .get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // Slot is available, proceed with booking
                db.collection("timetableBookings")
                    .document(bookingId)
                    .set(bookingData)
                    .addOnSuccessListener {
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        onFailure("Failed to save booking: ${e.message}")
                    }
            } else {
                onFailure("This time slot is already booked!")
            }
        }
        .addOnFailureListener { e ->
            onFailure("Error checking availability: ${e.message}")
        }
}

// Function to load booked slots for a specific date and venue type
fun loadBookedSlots(
    db: FirebaseFirestore,
    date: String,
    venueType: String,
    onLoaded: (Map<String, Boolean>) -> Unit
) {
    db.collection("timetableBookings")
        .whereEqualTo("date", date)
        .whereEqualTo("venueType", venueType)
        .get()
        .addOnSuccessListener { documents ->
            val slots = mutableMapOf<String, Boolean>()
            for (document in documents) {
                val slotKey = document.getString("slotKey") ?: continue
                slots[slotKey] = true
            }
            onLoaded(slots)
        }
        .addOnFailureListener {
            onLoaded(emptyMap())
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnderlinedFloatingLabelDropdown(
    label: String,
    value: String,
    items: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        if (value.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
            )
        }

        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = { if (value.isEmpty()) Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,

                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Gray,
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}