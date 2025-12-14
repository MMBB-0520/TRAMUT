package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.viewModel.FacilityViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddFacilityScreen(
    adminDepartment: String,
    onNavigateBack: () -> Unit,
    viewModel: FacilityViewModel = viewModel()
) {
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }

    // UI Loading state
    var isLoading by remember { mutableStateOf(false) }

    // --- DROPDOWN STATE ---
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isStartTimeExpanded by remember { mutableStateOf(false) }
    var isEndTimeExpanded by remember { mutableStateOf(false) }

    var categoryWidth by remember { mutableStateOf(0) }
    var startTimeWidth by remember { mutableStateOf(0) }
    var endTimeWidth by remember { mutableStateOf(0) }

    fun getHour(timeStr: String): Int {
        return if (timeStr.contains(":")) timeStr.split(":")[0].toIntOrNull() ?: 0 else 0
    }

    val allTimeSlots = remember { (8..22).map { String.format("%02d:00", it) } }

    val availableEndTimes = remember(viewModel.formStartTime.value) {
        val startHour = getHour(viewModel.formStartTime.value)
        if (startHour == 0) allTimeSlots else allTimeSlots.filter { getHour(it) > startHour }
    }

    val venueOptions = remember(adminDepartment) {
        val baseOptions = when (adminDepartment) {
            "Sport" -> listOf("Badminton Court", "Squash Court", "Gym")
            "Library" -> listOf("Discussion Room", "Discussion Room with PC", "Discussion Room (with LCD Projector & Whiteboard)", "Individual Study Room")
            else -> listOf("Discussion Room (1 PC)", "Discussion Room (2 PCs)", "Discussion Room with Projector (2 PCs)", "Hall", "Auditorium")
        }
        baseOptions + "Others"
    }

    var isCustomCategory by remember { mutableStateOf(false) }

    // --- CLOSURE STATE ---
    val specialClosuresMap = remember { mutableStateMapOf<String, List<Int>>() }
    var selectedDateForClosure by remember { mutableStateOf("") }
    val selectedHoursForClosure = remember { mutableStateListOf<Int>() }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, day: Int ->
            selectedDateForClosure = String.format("%d-%02d-%02d", year, month + 1, day)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    LaunchedEffect(Unit) { viewModel.clearForm() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Facility", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // --- 1. BASIC INFO ---
            LabeledInput("Facility / Department") { LineStaticInput(text = adminDepartment) }
            Spacer(Modifier.height(16.dp))

            LabeledInput("Facility Name") {
                LineTextField(
                    viewModel.formName.value,
                    { viewModel.formName.value = it },
                    placeholder = "e.g. Main Meeting Room"
                )
            }
            Spacer(Modifier.height(16.dp))

            // --- CATEGORY DROPDOWN ---
            LabeledInput("Category") {
                Column {
                    Box(modifier = Modifier.onSizeChanged { categoryWidth = it.width }) {
                        val displayText = if (isCustomCategory) "Others" else viewModel.formCategory.value.ifEmpty { "Select Category" }

                        LineStaticInput(
                            text = displayText,
                            onClick = { isCategoryExpanded = true },
                            icon = Icons.Default.ArrowDropDown
                        )

                        DropdownMenu(
                            expanded = isCategoryExpanded,
                            onDismissRequest = { isCategoryExpanded = false },
                            modifier = Modifier
                                .width(with(LocalDensity.current) { categoryWidth.toDp() })
                                .background(Color.White)
                                .heightIn(max = 250.dp)
                        ) {
                            venueOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        if (opt == "Others") {
                                            isCustomCategory = true
                                            if (venueOptions.contains(viewModel.formCategory.value)) {
                                                viewModel.formCategory.value = ""
                                            }
                                        } else {
                                            isCustomCategory = false
                                            viewModel.formCategory.value = opt
                                        }
                                        isCategoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (isCustomCategory) {
                        Spacer(Modifier.height(8.dp))
                        LineTextField(
                            value = viewModel.formCategory.value,
                            onValueChange = { viewModel.formCategory.value = it },
                            placeholder = "Type custom category here..."
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- TIME SELECTION ---
            Text("Default Operating Hours", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f)) {
                    LabeledInput("Opens At") {
                        Box(modifier = Modifier.onSizeChanged { startTimeWidth = it.width }) {
                            LineStaticInput(
                                text = viewModel.formStartTime.value.ifEmpty { "Select" },
                                onClick = { isStartTimeExpanded = true },
                                icon = Icons.Default.ArrowDropDown
                            )
                            DropdownMenu(
                                expanded = isStartTimeExpanded,
                                onDismissRequest = { isStartTimeExpanded = false },
                                modifier = Modifier
                                    .width(with(LocalDensity.current) { startTimeWidth.toDp() })
                                    .background(Color.White)
                                    .heightIn(max = 250.dp)
                            ) {
                                allTimeSlots.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = {
                                            val newStart = getHour(time)
                                            val currentEnd = getHour(viewModel.formEndTime.value)
                                            if (currentEnd > 0 && newStart >= currentEnd) {
                                                viewModel.formEndTime.value = ""
                                                Toast.makeText(context, "End time reset.", Toast.LENGTH_SHORT).show()
                                            }
                                            viewModel.formStartTime.value = time
                                            isStartTimeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Box(Modifier.weight(1f)) {
                    LabeledInput("Closes At") {
                        Box(modifier = Modifier.onSizeChanged { endTimeWidth = it.width }) {
                            val isEnabled = viewModel.formStartTime.value.isNotEmpty()

                            LineStaticInput(
                                text = viewModel.formEndTime.value.ifEmpty { if(isEnabled) "Select" else "Pick Start" },
                                onClick = { if(isEnabled) isEndTimeExpanded = true },
                                icon = Icons.Default.ArrowDropDown
                            )
                            DropdownMenu(
                                expanded = isEndTimeExpanded,
                                onDismissRequest = { isEndTimeExpanded = false },
                                modifier = Modifier
                                    .width(with(LocalDensity.current) { endTimeWidth.toDp() })
                                    .background(Color.White)
                                    .heightIn(max = 250.dp)
                            ) {
                                availableEndTimes.forEach { time ->
                                    DropdownMenuItem(
                                        text = { Text(time) },
                                        onClick = {
                                            viewModel.formEndTime.value = time
                                            isEndTimeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            Text("Special Closures / Maintenance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedDateForClosure.isEmpty()) "Select Date for Closure" else selectedDateForClosure,
                            fontWeight = if (selectedDateForClosure.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp
                        )
                    }
                    HorizontalDivider()

                    if (selectedDateForClosure.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Select Hours to CLOSE:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        val hoursList = (8..22).toList()
                        val columns = 4
                        val chunkedHours = hoursList.chunked(columns)

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chunkedHours.forEach { rowHours ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowHours.forEach { hour ->
                                        val isSelected = selectedHoursForClosure.contains(hour)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .background(
                                                    if (isSelected) Color.Red else Color(0xFFF0F0F0),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .clickable {
                                                    if (isSelected) selectedHoursForClosure.remove(hour)
                                                    else selectedHoursForClosure.add(hour)
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = String.format("%02d:00", hour),
                                                fontSize = 12.sp,
                                                color = if (isSelected) Color.White else Color.Black
                                            )
                                        }
                                    }
                                    repeat(columns - rowHours.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (selectedHoursForClosure.isNotEmpty()) {
                                    specialClosuresMap[selectedDateForClosure] = selectedHoursForClosure.toList()
                                    selectedHoursForClosure.clear()
                                    selectedDateForClosure = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Exception")
                        }
                    }
                }
            }

            if (specialClosuresMap.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Scheduled Closures:", fontWeight = FontWeight.Bold)

                specialClosuresMap.forEach { (date, hours) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(Color.White, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Date: $date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "Closed: " + hours.sorted().joinToString(", ") { String.format("%02d:00", it) },
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { specialClosuresMap.remove(date) }) {
                            Icon(Icons.Default.Delete, "Remove", tint = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- SUBMIT BUTTON WITH ERROR HANDLING ---
            Button(
                onClick = {
                    val start = getHour(viewModel.formStartTime.value)
                    val end = getHour(viewModel.formEndTime.value)

                    if (viewModel.formName.value.isBlank()) {
                        Toast.makeText(context, "Error: Facility Name is missing", Toast.LENGTH_SHORT).show()
                    } else if (viewModel.formCategory.value.isBlank()) {
                        Toast.makeText(context, "Error: Category is not selected", Toast.LENGTH_SHORT).show()
                    } else if (viewModel.formStartTime.value.isEmpty() || viewModel.formEndTime.value.isEmpty()) {
                        Toast.makeText(context, "Error: Operating hours are missing", Toast.LENGTH_SHORT).show()
                    } else if (start >= end) {
                        Toast.makeText(context, "Error: Start time must be before End time", Toast.LENGTH_SHORT).show()
                    } else {
                        // Start Loading
                        isLoading = true

                        // Handle Both Success and Failure
                        viewModel.addFacility(
                            department = adminDepartment,
                            specialClosures = specialClosuresMap.toMap()
                        ) { isSuccess, errorMessage ->

                            isLoading = false // STOP LOADING

                            if (isSuccess) {
                                showSuccessDialog = true
                            } else {
                                Toast.makeText(context, "Upload Failed: $errorMessage", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(50.dp))
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("OK", color = Color.White) }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Facility Added Successfully!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun LabeledInput(label: String, content: @Composable () -> Unit) {
    Column {
        Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
fun LineTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Black,
            unfocusedIndicatorColor = Color.Gray
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun LineStaticInput(text: String, onClick: () -> Unit = {}, icon: ImageVector? = null) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = if (text == "Select Category" || text == "Select" || text == "Pick Start") Color.Gray else Color.Black
                )
                if (icon != null) {
                    Icon(icon, null, tint = Color.Gray)
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.Gray)
        }
    }
}