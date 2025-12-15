package com.example.tramut.userInterface.studentTheme

import android.R.attr.label
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityChartScreen(
    selectedFacilityFromPrevious: String,
    onBookNow: (String, String) -> Unit = { _, _ -> },
    onBackFacility: () -> Unit = {}
) {
    // Date follow actual date
    val formatter = DateTimeFormatter.ofPattern("dd / MMM / yyyy (EEE)", Locale.ENGLISH)
    val today = LocalDate.now()

    // book for 3 days
    val dateList = remember {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd / MMM / yyyy (EEE)", Locale.ENGLISH)

        List(3) { i ->
            calendar.time = Date() // set to today
            calendar.add(Calendar.DAY_OF_YEAR, i)
            formatter.format(calendar.time)
        }
    }

    var selectedDate by remember { mutableStateOf("") }

    // Venue
    val venueList = when (selectedFacilityFromPrevious) {
        "Cyber Centre Discussion Room" -> listOf(
            "Discussion Room (1 PC)",
            "Discussion Room (2 PCs)",
            "Discussion Room (2 PCs)",
            "Discussion Room with Projector (2 PCs)",
            "Discussion Room with Projector (2 PCs) [HDMI]"
        )
        "Library Discussion Room" -> listOf(
            "Discussion Room",
            "Discussion Room with PC",
            "Individual Study Room",
            "Presentation Room(with LCD Projector & Whiteboard)"
        )
        "Sports Facilities" -> listOf(
            "Club House - Squash",
            "Club House-Swimming Pool",
            "Club House-Guest/Karaoke Room",
            "Club House-Gym 1 (11am-1pm, 3pm-5pm & 7pm-9pm)",
            "Club House-Gym 1 (9am-11am, 1pm-3pm & 5pm-7pm)",
            "Club House-S.P.Foyer (11am-1pm & 3pm-5pm)",
            "Club House-S.P.Foyer (9am-11am & 1pm-3pm)",
            "Club House-Snooker",
            "Sports Complex-Badminton",
            "Sports Complex-Gym 3 (11am-1pm, 3pm-5pm & 7pm-9pm)",
            "Sports Complex-Gym 3 (9am-11am, 1pm-3pm & 5pm-7pm)",
            "Sports Complex-Pickleball",
            "Sports Complex-Table Tennis",
            "Sports Complex-Tennis",
            "Sports Complex-Futsal",
            "TAR UMT Arena-Futsal"
        )
        else -> emptyList()
    }

    var selectedVenue by remember { mutableStateOf("") }

    // Update selectedVenue when venueOptions change
    LaunchedEffect(venueList) {
        selectedVenue = ""
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Book Now
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = { onBookNow(selectedVenue, selectedDate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1)
                )
            ) {
                Text("Book Now")
            }
        }

        // Booking Date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            UnderlinedFloatingLabelDropdown(
                label = "Booking Date *",
                value = selectedDate,
                items = dateList,
                onValueChange = { selectedDate = it }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Venue Type
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            UnderlinedFloatingLabelDropdown(
                label = "Venue Type *",
                value = selectedVenue,
                items = venueList,
                onValueChange = { selectedVenue = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.3f)
        )
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
    val hasValue = value.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Column {
                if (hasValue) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasValue) value else label,
                        fontSize = if (hasValue) 16.sp else 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (hasValue) Color.Black else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Gray
                    )
                }
            }
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            item,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}