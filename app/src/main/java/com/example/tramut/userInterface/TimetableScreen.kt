package com.example.tramut.userInterface

import android.R.attr.rowHeight
import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.viewModel.TimetableViewModel
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    initialDepartment: String,
    onNavigateBack: () -> Unit,
    onNavigateToBooking: (facilityId: String, hour: Int, date: String) -> Unit,
    viewModel: TimetableViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val categoryOptions = remember(initialDepartment) {
        val optionsList = when (initialDepartment) {
            "Sport Facilities" -> listOf("All Sport Facilities", "Badminton", "Squash", "Gym", "Guest/Karaoke Room", "Swimming Pool", "Snooker", "Pickleball", "Table Tennis", "Tennis", "Futsal")
            "Library" -> listOf("All Library Rooms", "Discussion Room", "Discussion Room with PC", "Individual Study Room")
            "CITC" -> listOf("All CITC Facilities", "Discussion Room (1 PC)", "Discussion Room (2 PCs)", "Discussion Room with Projector (2 PCs)", "Discussion Room with Projector (2 PCs)[HDMI]")
            else -> listOf("All $initialDepartment Facilities")
        }
        optionsList
    }

    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }

    LaunchedEffect(categoryOptions) {
        selectedCategory = categoryOptions.first()
    }

    LaunchedEffect(selectedCategory) {
        val facilityQuery = if (selectedCategory.startsWith("All")) {
            initialDepartment
        } else {
            selectedCategory
        }
        val isCategoryQuery = !selectedCategory.startsWith("All")

        viewModel.fetchTimetableData(
            identifier = facilityQuery,
            isCategory = isCategoryQuery,
            date = uiState.selectedDate // Use the date initialized in the VM
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Timetable", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFFEEEEF2)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    // Category Dropdown
                    DepartmentDropdownLineStyle(
                        currentSelection = selectedCategory,
                        options = categoryOptions,
                        onSelect = { selectedCategory = it }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    // Date Picker
                    AdminDatePickerLineStyle(
                        currentDate = uiState.selectedDate,
                        // FIX: Changed onDateSelected to call the VM's updateDate, which handles booking refresh
                        onDateSelected = { newDate -> viewModel.updateDate(newDate) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text("Error: ${uiState.errorMessage}",
                        color = Color.Red)
                }
            }
            else {

                TimetableGrid(
                    facilities = uiState.facilitiesList,
                    viewModel = viewModel,
                    onCellClick = { _, hour -> 
                        val assignedId = viewModel.autoAssignFacilityId(
                            category = selectedCategory,
                            date = uiState.selectedDate,
                            hour = hour
                        )

                        if (assignedId != null) {
                            onNavigateToBooking(assignedId, hour, uiState.selectedDate)
                        } else {
                            Log.e("Booking", "No available facility found for this slot.")
                        }
                    }
                )

                Spacer(Modifier.height(10.dp))
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

// --- HELPER COMPONENT: The Grid (Unchanged) ---
@Composable
fun TimetableGrid(
    facilities: List<Facility>,
    viewModel: TimetableViewModel,
    onCellClick: (Facility, Int) -> Unit // Added this to handle booking navigation
) {
    val venueColWidth = 130.dp
    val timeColWidth = 60.dp
    val rowHeight = 35.dp
    val borderColor = Color(0xFFE0E0E0)

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column {
        // --- HEADER ROW (Labels only) ---
        Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
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

            // Correct Header Logic: Just show the time strings
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

        // --- DATA ROWS (Where the facilities and colored boxes are) ---
        Column(
            modifier = Modifier
                .verticalScroll(verticalScroll)
                .fillMaxHeight()
        ) {
            facilities.forEach { facility ->
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    // 1. Facility Name Column
                    Box(
                        modifier = Modifier
                            .width(venueColWidth)
                            .height(rowHeight)
                            .background(Color.White)
                            .border(1.dp, borderColor),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = facility.name,
                            modifier = Modifier.padding(horizontal = 4.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // 2. Status Cells (Colored Boxes)
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
                                // Enable clicking only if the slot is "Available"
                                .clickable(enabled = status == "Available") {
                                    onCellClick(facility, hour)
                                }
                        )
                    }
                }
            }
        }
    }
}

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
                .background(Color.White)
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

// --- DATE PICKER COMPONENT (Unchanged) ---
@Composable
fun AdminDatePickerLineStyle(currentDate: String, onDateSelected: (String) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val parts = currentDate.split("-")

    if (parts.size == 3) {
        try {
            calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
        } catch (e: NumberFormatException) {
            Log.e("DatePicker", "Invalid date format: $currentDate")
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            onDateSelected(String.format("%d-%02d-%02d", year, month + 1, day))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        TextField(
            value = currentDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date", fontSize = 12.sp) },
            trailingIcon = { Icon(Icons.Default.DateRange, "Select") },
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
        // Clickable layer over TextField to show DatePickerDialog
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { datePickerDialog.show() }
        )
    }
}

// --- LEGEND COMPONENT (Unchanged) ---
@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, fontSize = 12.sp, color = Color.Black)
    }
}