package com.example.tramut.userInterface.studentTheme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.tramut.userInterface.DepartmentDropdownLineStyle
import com.example.tramut.viewModel.TimetableViewModel
import com.example.tramut.userInterface.LegendItem
import com.example.tramut.userInterface.formatDateForDisplay
import com.example.tramut.userInterface.formatForFirebase
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailabilityChartScreen(
    selectedFacilityFromPrevious: String,
    viewModel: TimetableViewModel = viewModel(),
    onBookNow: (String, String) -> Unit = { _, _ -> },
    onBackFacility: () -> Unit = {}
) {
    // Reuse the date logic
    val today = java.time.LocalDate.now()

    // Generate next 3 days for display
    val dateList = remember {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy / MMM / dd (EEE)", Locale.ENGLISH)

        List(3) { i ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, i)
            formatter.format(calendar.time)
        }
    }

    var selectedDate by remember { mutableStateOf("") }
    var selectedVenue by remember { mutableStateOf("") }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()

    // Reuse category options logic
    val categoryOptions = remember(selectedFacilityFromPrevious) {
        when (selectedFacilityFromPrevious) {
            "Sports", "Sports Facilities" -> listOf(
                "Badminton",
                "Squash",
                "Gym",
                "Guest/Karaoke Room",
                "Swimming Pool",
                "Snooker",
                "Pickleball",
                "Table Tennis",
                "Tennis",
                "Futsal"
            )

            "Library", "Library Discussion Room" -> listOf(
                "Discussion Room",
                "Discussion Room with PC",
                "Individual Study Room"
            )

            "Cyber Centre", "Cyber Centre Discussion Room" -> listOf(
                "Discussion Room (1 PC)",
                "Discussion Room (2 PCs)",
                "Discussion Room with Projector (2 PCs)"
            )

            else -> listOf("All $selectedFacilityFromPrevious Facilities")
        }
    }

    var selectedCategory by remember { mutableStateOf("") }

    // Reset selectedCategory when options change
    LaunchedEffect(categoryOptions) {
        selectedCategory = categoryOptions.first()
    }

    LaunchedEffect(selectedCategory, selectedDate) {
        val facilityQuery = if (selectedCategory.startsWith("All")) {
            selectedFacilityFromPrevious
        } else {
            selectedCategory
        }
        val isCategoryQuery = !selectedCategory.startsWith("All")
        val firebaseFormattedDate = formatForFirebase(selectedDate)

        // 1. Load the list of courts/rooms
        viewModel.fetchTimetableData(
            identifier = facilityQuery,
            isCategory = isCategoryQuery,
            date = firebaseFormattedDate
        )

        // 2. Start watching for Blue squares (Bookings)
        viewModel.listenToBookingsForDate(firebaseFormattedDate)
    }

    // Initialize with first date
    LaunchedEffect(Unit) {
        if (dateList.isNotEmpty()) {
            selectedDate = dateList[0]
        }
    }

    val canBook = selectedDate.isNotEmpty() && selectedVenue.isNotEmpty()

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Book Now Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Button(
                onClick = {
                    onBookNow(selectedVenue, selectedDate)
                },
                enabled =true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1),
                    disabledContainerColor = Color(0xFFB0BEC5)
                )
            ) {
                Text("Book Now")
            }

        }

        // Date selector - Custom 3-day selector for AvailabilityChartScreen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 8.dp)
        ) {
            UnderlinedFloatingLabelDropdown(
                label = "Booking Date *",
                value = selectedDate,
                items = dateList,
                onValueChange = {
                    selectedDate = it
                    selectedVenue = "" // Clear venue selection when date changes
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Dropdown (Reused from TimetableScreen)
        if (categoryOptions.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 8.dp)
            ) {
                DepartmentDropdownLineStyle(
                    currentSelection = selectedCategory,
                    options = categoryOptions,
                    onSelect = {
                        selectedCategory = it
                        selectedVenue = "" // Clear venue selection when category changes
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Selected Venue Display
        if (selectedVenue.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selected venue: ",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = selectedVenue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0D47A1)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Main Timetable Section
        Column(modifier = Modifier.weight(1f)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Timetable",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Show current selected category
                Text(
                    text = selectedCategory,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Loading/Error States
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.errorMessage}", color = Color.Red)
                }
            } else {
                // Timetable Grid
                AvailabilityChartTimetableGrid(
                    facilities = uiState.facilitiesList,
                    viewModel = viewModel,
                    selectedVenue = selectedVenue,
                    onVenueSelected = { venueName ->
                        selectedVenue = if (selectedVenue == venueName) "" else venueName
                    }
                )

                Spacer(Modifier.height(10.dp))

                // Legend
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(Color(0xFF4CAF50), "Available")
                        LegendItem(Color(0xFF2196F3), "Booked")
                        LegendItem(Color(0xFFF44336), "Maint.")
                        LegendItem(Color(0xFFE0E0E0), "Closed")
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilityChartTimetableGrid(
    facilities: List<Facility>,
    viewModel: TimetableViewModel,
    selectedVenue: String,
    onVenueSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val venueColWidth = 130.dp
    val timeColWidth = 60.dp
    val rowHeight = 35.dp
    val borderColor = Color(0xFFE0E0E0)

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(modifier = modifier.background(MaterialTheme.colorScheme.background)) {
        // Header Row
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            Box(
                modifier = Modifier
                    .width(venueColWidth)
                    .height(rowHeight)
                    .background(MaterialTheme.colorScheme.background)
                    .border(1.dp, borderColor),
                contentAlignment = Alignment.Center
            ) {
                Text("Venue/Time", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Time Slots from 8:00 to 22:00
            (8..22).forEach { hour ->
                Box(
                    modifier = Modifier
                        .width(timeColWidth)
                        .height(rowHeight)
                        .background(MaterialTheme.colorScheme.background)
                        .border(1.dp, borderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d:00", hour),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Data Rows
        Column(
            modifier = Modifier
                .verticalScroll(verticalScroll)
                .background(MaterialTheme.colorScheme.background)
                .fillMaxHeight()
        ) {
            facilities.forEach { facility ->
                val isSelected = selectedVenue == facility.name

                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    // Facility Name Column - Clickable
                    Box(
                        modifier = Modifier
                            .width(venueColWidth)
                            .height(rowHeight)
                            .background(
                                if (isSelected) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.background
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0D47A1) else borderColor
                            )
                            .clickable {
                                onVenueSelected(facility.name)
                            },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = facility.name,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            lineHeight = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) Color(0xFF0D47A1) else MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Status Cells
                    (8..22).forEach { hour ->
                        val status = viewModel.getSlotStatus(facility, hour)
                        val cellColor = when (status) {
                            "Available" -> Color(0xFF4CAF50) // Green
                            "Booked" -> Color(0xFF2196F3)    // Blue
                            "Maintenance" -> Color(0xFFF44336) // Red
                            else -> Color(0xFFE0E0E0)        // Gray (Closed/Unknown)
                        }

                        Box(
                            modifier = Modifier
                                .width(timeColWidth)
                                .height(rowHeight)
                                .background(cellColor)
                                .border(0.5.dp, Color.White)
                        )
                    }
                }
            }
        }
    }
}

// Custom dropdown for AvailabilityChartScreen (Unique - shows next 3 days)
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
                        text = if (hasValue) formatDateForDisplay(value) else label,
                        fontSize = if (hasValue) 16.sp else 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (hasValue) MaterialTheme.colorScheme.onBackground else Color.Gray,
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
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            formatDateForDisplay(item),
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

// Department Dropdown (reused from TimetableScreen)
@Composable
fun DepartmentDropdownLineStyle(
    currentSelection: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dropdownWidth = remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                dropdownWidth.value = with(density) { coordinates.size.width.toDp() }
            }
    ) {
        TextField(
            value = currentSelection,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category", fontSize = 12.sp) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Gray,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Gray
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(dropdownWidth.value)
                .heightIn(max = 200.dp)
                .background(MaterialTheme.colorScheme.background)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}