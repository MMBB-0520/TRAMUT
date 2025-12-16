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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.viewModel.TimetableViewModel
import com.example.tramut.userInterface.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    val formatter = DateTimeFormatter.ofPattern("dd / MMM / yyyy (EEE)", Locale.ENGLISH)
    val today = LocalDate.now()

    // Generate next 3 days for display
    val dateList = remember {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("dd / MMM / yyyy (EEE)", Locale.ENGLISH)

        List(3) { i ->
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, i)
            formatter.format(calendar.time)
        }
    }

    var selectedDate by remember { mutableStateOf(dateList.firstOrNull() ?: "") }
    var selectedFormattedDate by remember { mutableStateOf("") }

    // Reuse category options logic
    val categoryOptions = remember(selectedFacilityFromPrevious) {
        when (selectedFacilityFromPrevious) {
            "Sport Facilities", "Sports Facilities" -> listOf(
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

            "CITC", "Cyber Centre Discussion Room" -> listOf(
                "Discussion Room (1 PC)",
                "Discussion Room (2 PCs)",
                "Discussion Room with Projector (2 PCs)"
            )
            else -> listOf("All $selectedFacilityFromPrevious Facilities")
        }
    }

    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var selectedVenue by remember { mutableStateOf("") }

    // Collect UI state
    val uiState by viewModel.uiState.collectAsState()
    val isLoading = uiState.isLoading
    val errorMessage = uiState.errorMessage

    // Date format converters
    val convertToViewModelFormat = { displayDate: String ->
        try {
            val displayFormat = SimpleDateFormat("dd / MMM / yyyy (EEE)", Locale.ENGLISH)
            val viewModelFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val date = displayFormat.parse(displayDate)
            date?.let { viewModelFormat.format(it) } ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    // Initialize
    LaunchedEffect(Unit) {
        if (dateList.isNotEmpty()) {
            selectedDate = dateList[0]
            selectedFormattedDate = convertToViewModelFormat(dateList[0])
        }
    }

    // Update ViewModel when date changes
    LaunchedEffect(selectedDate) {
        val vmDate = convertToViewModelFormat(selectedDate)
        if (vmDate.isNotEmpty()) {
            selectedFormattedDate = vmDate
            viewModel.updateDate(vmDate)
        }
    }

    // Load facilities when category or date changes
    LaunchedEffect(selectedCategory, selectedFormattedDate) {
        if (selectedCategory.startsWith("All")) {
            viewModel.loadFacilities(selectedFacilityFromPrevious, isCategory = false)
        } else {
            viewModel.loadFacilities(selectedCategory, isCategory = true)
        }

        if (selectedFormattedDate.isNotEmpty()) {
            viewModel.loadBookingsForDate(selectedFormattedDate)
        }
    }

    // Reset category when facility changes
    LaunchedEffect(categoryOptions) {
        selectedCategory = categoryOptions.first()
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Book Now Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // --- 关键改动: 检查 selectedVenue 是否为空 ---
            val isVenueSelected = selectedVenue.isNotEmpty()

            Button(
                onClick = {
                    onBookNow(selectedVenue, selectedDate)
                },
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0D47A1)
                )
            ) {
                Text("Book Now")
            }

        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom Date Selector (Unique to AvailabilityChartScreen - shows next 3 days)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            UnderlinedFloatingLabelDropdown(
                label = "Booking Date *",
                value = selectedDate,
                items = dateList,
                onValueChange = {
                    selectedDate = it
                }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Category Dropdown (Reused from TimetableScreen)
        if (categoryOptions.size > 1) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                DepartmentDropdownLineStyle(
                    currentSelection = selectedCategory,
                    options = categoryOptions,
                    onSelect = { selectedCategory = it }
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }else {
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Divider
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            thickness = 1.dp,
            color = Color.Gray.copy(alpha = 0.3f)
        )

        // 显示选择的venue
        if (selectedVenue.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
        }

        // Main Timetable Section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Timetable",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // 显示当前选择的category
                Text(
                    text = selectedCategory,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reuse the main display logic from TimetableScreen
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Loading timetable...")
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: $errorMessage",
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (selectedCategory.startsWith("All")) {
                                    viewModel.loadFacilities(selectedFacilityFromPrevious, false)
                                } else {
                                    viewModel.loadFacilities(selectedCategory, true)
                                }
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else if (uiState.facilitiesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No facilities available")
                }
            } else {
                // 使用可以選擇venue的TimetableGrid
                AvailabilityChartTimetableGrid(
                    facilities = uiState.facilitiesList,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                    onVenueSelected = { venueName ->
                        selectedVenue = venueName
                    }
                )

                Spacer(Modifier.height(10.dp))

                // REUSE Legend from TimetableScreen
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

// Custom TimetableGrid for AvailabilityChartScreen
@Composable
fun AvailabilityChartTimetableGrid(
    facilities: List<com.example.myfacilitybookingsystem.rooms.entity.Facility>,
    viewModel: com.example.myfacilitybookingsystem.viewModel.TimetableViewModel,
    modifier: Modifier = Modifier,
    onVenueSelected: (String) -> Unit
) {
    val venueColWidth = 130.dp
    val timeColWidth = 60.dp
    val rowHeight = 35.dp
    val borderColor = Color(0xFFE0E0E0)
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column(modifier = modifier) {
        // --- HEADER ROW ---
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
            // Corner Box
            Box(
                modifier = Modifier
                    .width(venueColWidth)
                    .height(rowHeight)
                    .background(Color.White)
                    .border(1.dp, borderColor),
                contentAlignment = Alignment.Center
            ) {
                Text("Venue/Time", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            // Time Headers
            (8..22).forEach { hour ->
                Box(
                    modifier = Modifier
                        .width(timeColWidth)
                        .height(rowHeight)
                        .background(Color.White)
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

        // --- DATA ROWS ---
        Column(
            modifier = Modifier
                .verticalScroll(verticalScroll)
        ) {
            facilities.forEach { facility ->
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    // Venue Name - 讓它可以點擊
                    Box(
                        modifier = Modifier
                            .width(venueColWidth)
                            .height(rowHeight)
                            .background(Color.White)
                            .border(1.dp, borderColor)
                            .clickable {
                                // 點擊venue名稱時，選擇這個venue
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
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Cells
                    (8..22).forEach { hour ->
                        val status = viewModel.getSlotStatus(facility, hour)
                        val cellColor = when (status) {
                            "Available" -> Color(0xFF4CAF50)
                            "Booked" -> Color(0xFF2196F3)
                            "Maintenance" -> Color(0xFFF44336)
                            else -> Color(0xFFE0E0E0)
                        }

                        Box(
                            modifier = Modifier
                                .width(timeColWidth)
                                .height(rowHeight)
                                .background(cellColor)
                                .border(0.5.dp, Color.White)
                                .clickable {
                                    // 時間格子也可以點擊
                                    if (status == "Available") {
                                        onVenueSelected(facility.name)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

// Custom dropdown for AvailabilityChartScreen (Unique)
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
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Default.ArrowDropDown,
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

// Legend item component
@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, androidx.compose.foundation.shape.CircleShape)
                .border(1.dp, Color.Gray, androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Black)
    }
}