package com.example.tramut.userInterface

import android.app.DatePickerDialog
import android.os.Build
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
    viewModel: TimetableViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Dynamic Category Logic
    val categoryOptions = remember(initialDepartment) {
        when (initialDepartment) {
            "Sport" -> listOf("All Sport Facilities", "Badminton Court", "Squash Court", "Gym")
            "Library" -> listOf("All Library Rooms", "Discussion Room", "Discussion Room with PC", "Discussion Room (with LCD Projector & Whiteboard)", "Individual Study Room")
            else -> listOf("All Discussion Room", "Discussion Room (1 PC)", "Discussion Room (2 PCs)", "Discussion Room with Projector (2 PCs)", "Discussion Room with Projector (2 PCs)[HDMI]")
        }
    }

    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }

    LaunchedEffect(selectedCategory) {
        if (selectedCategory.startsWith("All")) {
            viewModel.loadFacilities(initialDepartment)
        } else {
            viewModel.loadFacilities(selectedCategory)
        }
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

            // --- CHANGED: Side-by-Side (In-Line) Layout ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp) // Space between inputs
            ) {
                // Left: Category
                Box(modifier = Modifier.weight(1f)) {
                    DepartmentDropdownLineStyle(
                        currentSelection = selectedCategory,
                        options = categoryOptions,
                        onSelect = { selectedCategory = it }
                    )
                }

                // Right: Date
                Box(modifier = Modifier.weight(1f)) {
                    AdminDatePickerLineStyle(
                        currentDate = uiState.selectedDate,
                        onDateSelected = { newDate -> viewModel.updateDate(newDate) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- Timetable Grid ---
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Black)
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    TimetableGrid(
                        facilities = uiState.facilitiesList,
                        viewModel = viewModel
                    )
                }

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

// --- HELPER COMPONENT: THE GRID ---
@Composable
fun TimetableGrid(
    facilities: List<Facility>,
    viewModel: TimetableViewModel
) {
    val venueColWidth = 130.dp
    val timeColWidth = 60.dp
    val rowHeight = 35.dp
    val borderColor = Color(0xFFE0E0E0)

    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Column {
        // Header Row
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

        // Data Rows
        Column(modifier = Modifier.verticalScroll(verticalScroll)) {
            facilities.forEach { facility ->
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
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
                            lineHeight = 11.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

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
                        )
                    }
                }
            }
        }
    }
}

// --- UPDATED DROPDOWN (LINE STYLE) ---
@Composable
fun DepartmentDropdownLineStyle(
    currentSelection: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Using TextField instead of OutlinedTextField for "Line" style
        TextField(
            value = currentSelection,
            onValueChange = {},
            readOnly = true,
            label = { Text("Category", fontSize = 12.sp) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, "Select") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // These colors make the box transparent, leaving only the bottom line
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

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
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

// --- UPDATED DATE PICKER (LINE STYLE) ---
@Composable
fun AdminDatePickerLineStyle(currentDate: String, onDateSelected: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val calendar = Calendar.getInstance()
    val parts = currentDate.split("-")
    if (parts.size == 3) calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            onDateSelected(String.format("%d-%02d-%02d", year, month + 1, day))
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // Using TextField instead of OutlinedTextField for "Line" style
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { datePickerDialog.show() }
        )
    }
}

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