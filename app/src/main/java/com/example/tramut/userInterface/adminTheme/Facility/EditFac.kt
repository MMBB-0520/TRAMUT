package com.example.tramut.userInterface.adminTheme.Facility

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.viewModel.FacilityViewModel
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditFacilityScreen(
    adminDepartment: String,
    onNavigateBack: () -> Unit,
    viewModel: FacilityViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedFacility by remember { mutableStateOf<Facility?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showDeleteFacilityDialog by remember { mutableStateOf(false) }
    var closureToDelete by remember { mutableStateOf<String?>(null) }

    // --- State for Data Model ---
    val dailyBreakHours = remember { mutableStateListOf<Int>() }
    val specialClosuresMap = remember { mutableStateMapOf<String, List<Int>>() }

    // --- State for Closure Input UI ---
    var isRangeMode by remember { mutableStateOf(false) }
    var specialClosureSingleDate by remember { mutableStateOf("") }
    val selectedHoursForClosure = remember { mutableStateListOf<Int>() }
    var specialClosureStartDate by remember { mutableStateOf("") }
    var specialClosureEndDate by remember { mutableStateOf("") }

    // Dropdown States for UI control
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isStartTimeExpanded by remember { mutableStateOf(false) }
    var isEndTimeExpanded by remember { mutableStateOf(false) }
    var categoryWidth by remember { mutableStateOf(0) }
    var startTimeWidth by remember { mutableStateOf(0) }
    var endTimeWidth by remember { mutableStateOf(0) }

    val density = LocalDensity.current

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val calendar = Calendar.getInstance()

    // --- Helper: Date Picker ---
    fun showDatePicker(onDateSelected: (String) -> Unit) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                onDateSelected(String.format("%d-%02d-%02d", year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // --- Time Slot and Hour Helpers ---
    val timeSlots = remember { (8..22).map { String.format("%02d:00", it) } }
    fun getHour(timeStr: String): Int = if (timeStr.contains(":")) timeStr.split(":")[0].toIntOrNull() ?: 8 else 8
    val start = getHour(viewModel.formStartTime.value)
    val end = getHour(viewModel.formEndTime.value)

    val availableEndTimes = remember(viewModel.formStartTime.value) {
        if (start == 0) timeSlots else timeSlots.filter { getHour(it) > start }
    }
    val allOperationalHours = remember { (8..22).toList() }
    // ------------------------------------

    val categoryOptions = remember(adminDepartment) {
        val baseOptions = when (adminDepartment) {
            "Sports" -> listOf(
                "Badminton", "Squash", "Gym", "Guest/Karaoke Room", "Swimming Pool", "Snooker", "Pickleball", "Table Tennis", "Tennis", "Futsal"
            )
            "Library" -> listOf("Discussion Room", "Discussion Room with PC", "Individual Study Room")
            "Cyber Center" -> listOf("Discussion Room (1 PC)", "Discussion Room (2 PCs)", "Discussion Room with Projector (2 PCs)", "Discussion Room with Projector (2 PCs)[HDMI]")
            else -> emptyList()
        }
        baseOptions + "Others"
    }
    var isCustomCategory by remember { mutableStateOf(false) }


    LaunchedEffect(adminDepartment) {
        viewModel.loadFacilities(adminDepartment)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Facility", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {

            // =================================================================
            // --- 1. SELECTION SECTION (RETAIN CARD) ---
            // =================================================================
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Select Facility to Edit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))

                    FacilityDropdownSelector(
                        currentSelection = selectedFacility?.name ?: "Select Facility",
                        options = viewModel.facilityList,
                        onSelect = { facility ->
                            try {
                                selectedFacility = facility
                                viewModel.selectFacilityForEdit(facility)
                                dailyBreakHours.clear()
                                dailyBreakHours.addAll(facility.dailyBreakHours)
                                specialClosuresMap.clear()
                                specialClosuresMap.putAll(facility.specialClosures)
                                specialClosureSingleDate = ""; specialClosureStartDate = ""; specialClosureEndDate = ""
                                selectedHoursForClosure.clear()
                                isRangeMode = false
                                Toast.makeText(context, "Facility loaded for editing", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error selecting facility", Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }


            if (selectedFacility != null) {

                Text("Facility Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))

                LabeledInput("Facility / Department") { LineStaticInput(text = adminDepartment) }
                Spacer(Modifier.height(16.dp))

                LabeledInput("Facility Name") {
                    LineTextField(viewModel.formName.value, { viewModel.formName.value = it }, "e.g. Court 1")
                }
                Spacer(Modifier.height(16.dp))

                // Category Dropdown Logic (Matching AdminAddFacilityScreen)
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
                                modifier = Modifier.width(with(density) { categoryWidth.toDp() }).background(Color.White).heightIn(max = 250.dp)
                            ) {
                                categoryOptions.forEach { opt ->
                                    DropdownMenuItem(text = { Text(opt) }, onClick = {
                                        isCustomCategory = (opt == "Others")
                                        viewModel.formCategory.value = if (opt == "Others") "" else opt
                                        isCategoryExpanded = false
                                    })
                                }
                            }
                        }
                        if (isCustomCategory) {
                            Spacer(Modifier.height(8.dp))
                            LineTextField(viewModel.formCategory.value, { viewModel.formCategory.value = it }, "Type custom category...")
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (adminDepartment != "Sports") {
                    LabeledInput("Capacity (List, e.g., 4, 5, 6)") {
                        LineTextField(
                            value = viewModel.formCapacity.value,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == ',' || it == ' ' }) {
                                    viewModel.formCapacity.value = newValue
                                }
                            },
                            placeholder = "Enter numbers separated by commas"
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // =================================================================
                // --- 3. OPERATING HOURS & DAILY BREAKS (NO CARD - LINE STYLE) ---
                // =================================================================
                Text("Operating Hours (Daily Schedule)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Start Time Dropdown Logic (Matching AdminAddFacilityScreen)
                    Box(Modifier.weight(1f)) {
                        LabeledInput("Opens At") {
                            Box(modifier = Modifier.onSizeChanged { startTimeWidth = it.width }) {
                                LineStaticInput(
                                    viewModel.formStartTime.value.ifEmpty { "Select" },
                                    { isStartTimeExpanded = true },
                                    Icons.Default.ArrowDropDown
                                )
                                DropdownMenu(
                                    expanded = isStartTimeExpanded,
                                    onDismissRequest = { isStartTimeExpanded = false },
                                    modifier = Modifier.width(with(density) { startTimeWidth.toDp() }).background(Color.White).heightIn(max = 250.dp)
                                ) {
                                    timeSlots.forEach { time ->
                                        DropdownMenuItem(text = { Text(time) }, onClick = {
                                            viewModel.formStartTime.value = time
                                            dailyBreakHours.clear()
                                            isStartTimeExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                    }
                    // End Time Dropdown Logic (Matching AdminAddFacilityScreen)
                    Box(Modifier.weight(1f)) {
                        LabeledInput("Closes At") {
                            Box(modifier = Modifier.onSizeChanged { endTimeWidth = it.width }) {
                                val isEnabled = viewModel.formStartTime.value.isNotEmpty()
                                LineStaticInput(
                                    viewModel.formEndTime.value.ifEmpty { "Select" },
                                    { if (isEnabled) isEndTimeExpanded = true },
                                    Icons.Default.ArrowDropDown
                                )
                                DropdownMenu(
                                    expanded = isEndTimeExpanded,
                                    onDismissRequest = { isEndTimeExpanded = false },
                                    modifier = Modifier.width(with(density) { endTimeWidth.toDp() }).background(Color.White).heightIn(max = 250.dp)
                                ) {
                                    availableEndTimes.forEach { time ->
                                        DropdownMenuItem(text = { Text(time) }, onClick = {
                                            viewModel.formEndTime.value = time
                                            dailyBreakHours.clear()
                                            isEndTimeExpanded = false
                                        })
                                    }
                                }
                            }
                        }
                    }
                }

                // --- DAILY BREAK FUNCTION (Matching AdminAddFacilityScreen) ---
                if (end > start) {
                    Spacer(Modifier.height(16.dp))
                    Text("Set Daily Break (Optional)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text("Select hours to be CLOSED every day", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))

                    val operationalHours = (start until end).toList()
                    val columns = 4
                    val chunked = operationalHours.chunked(columns)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunked.forEach { rowHours ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                rowHours.forEach { hour ->
                                    val isBreak = dailyBreakHours.contains(hour)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(if (isBreak) Color.Red else Color.White, RoundedCornerShape(4.dp))
                                            .border(1.dp, if (isBreak) Color.Red else Color.LightGray, RoundedCornerShape(4.dp))
                                            .clickable { if (isBreak) dailyBreakHours.remove(hour) else dailyBreakHours.add(hour) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = String.format("%02d:00", hour), fontSize = 12.sp, color = if (isBreak) Color.White else Color.Black, fontWeight = if (isBreak) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                                repeat(columns - rowHours.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // =================================================================
                // --- 4. SPECIAL CLOSURES (RETAIN CARD) ---
                // =================================================================
                Text("Special Closures (Exceptions)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Mode Toggle (Matching AdminAddFacilityScreen)
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFEEEEF2), RoundedCornerShape(8.dp)).padding(4.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).background(if(!isRangeMode) Color.White else Color.Transparent, RoundedCornerShape(6.dp)).clickable { isRangeMode = false }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text("Specific Hours", fontWeight = FontWeight.Bold, color = if(!isRangeMode) Color.Black else Color.Gray, fontSize = 12.sp)
                            }
                            Box(modifier = Modifier.weight(1f).background(if(isRangeMode) Color.White else Color.Transparent, RoundedCornerShape(6.dp)).clickable { isRangeMode = true }.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text("Date Range (Full Day)", fontWeight = FontWeight.Bold, color = if(isRangeMode) Color.Black else Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        if (!isRangeMode) {
                            // --- SINGLE DAY / SPECIFIC HOURS MODE (Matching AdminAddFacilityScreen) ---
                            LabeledInput("Select Date") {
                                // Using standard Row/Icon/Text structure which mimics LineStaticInput for the date picker
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker { specialClosureSingleDate = it } }.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.DateRange, null, tint = Color.Gray)
                                    Spacer(Modifier.width(8.dp))
                                    Text(specialClosureSingleDate.ifEmpty { "Pick a date" }, fontSize = 16.sp, fontWeight = if(specialClosureSingleDate.isNotEmpty()) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                            HorizontalDivider(color = Color.Gray) // Added divider to complete line style

                            if (specialClosureSingleDate.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text("Select Hours to CLOSE (e.g. Competition):", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.height(8.dp))

                                val hoursList = (8..22).toList()
                                val chunkedHours = hoursList.chunked(4)

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    chunkedHours.forEach { rowHours ->
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            rowHours.forEach { hour ->
                                                val isSelected = selectedHoursForClosure.contains(hour)
                                                Box(
                                                    modifier = Modifier.weight(1f).background(if (isSelected) Color.Red else Color(0xFFF0F0F0), RoundedCornerShape(4.dp)).clickable { if (isSelected) selectedHoursForClosure.remove(hour) else selectedHoursForClosure.add(hour) }.padding(vertical = 8.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(String.format("%02d:00", hour), fontSize = 12.sp, color = if (isSelected) Color.White else Color.Black)
                                                }
                                            }
                                            repeat(4 - rowHours.size) { Spacer(Modifier.weight(1f)) }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text("Close Facility for a period of time (Full Days).", fontSize = 12.sp, color = Color.Gray)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    LabeledInput("From") {
                                        LineStaticInput(
                                            text = specialClosureStartDate.ifEmpty { "Start Date" },
                                            onClick = { showDatePicker { specialClosureStartDate = it } },
                                            icon = Icons.Default.DateRange
                                        )
                                    }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    LabeledInput("To") {
                                        LineStaticInput(
                                            text = specialClosureEndDate.ifEmpty { "End Date" },
                                            onClick = { showDatePicker { specialClosureEndDate = it } },
                                            icon = Icons.Default.DateRange
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (!isRangeMode) {
                                    if (specialClosureSingleDate.isNotEmpty() && selectedHoursForClosure.isNotEmpty()) {
                                        specialClosuresMap[specialClosureSingleDate] = selectedHoursForClosure.toList()
                                        selectedHoursForClosure.clear(); specialClosureSingleDate = ""
                                        Toast.makeText(context, "Added specific hours closure", Toast.LENGTH_SHORT).show()
                                    } else Toast.makeText(context, "Select date & hours", Toast.LENGTH_SHORT).show()
                                } else {
                                    if (specialClosureStartDate.isNotEmpty() && specialClosureEndDate.isNotEmpty()) {
                                        try {
                                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                            val startD = sdf.parse(specialClosureStartDate)
                                            val endD = sdf.parse(specialClosureEndDate)
                                            if (startD != null && endD != null && !startD.after(endD)) {
                                                val cal = Calendar.getInstance(); cal.time = startD
                                                while (!cal.time.after(endD)) {
                                                    specialClosuresMap[sdf.format(cal.time)] = allOperationalHours // Full Day Closure
                                                    cal.add(Calendar.DATE, 1)
                                                }
                                                specialClosureStartDate = ""; specialClosureEndDate = ""
                                                Toast.makeText(context, "Added Range Closure", Toast.LENGTH_SHORT).show()
                                            } else Toast.makeText(context, "Invalid Date Range", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) { Toast.makeText(context, "Error parsing dates", Toast.LENGTH_SHORT).show() }
                                    } else Toast.makeText(context, "Select Start & End dates", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp))
                            Text(if(isRangeMode) "Add Date Range Closure" else "Add Closure Exception")
                        }
                    }
                }

                if (specialClosuresMap.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Scheduled Closures:", fontWeight = FontWeight.Bold)
                    specialClosuresMap.toSortedMap().forEach { (date, hours) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(Color.White, RoundedCornerShape(4.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Date: $date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                val isFullDay = hours.containsAll(allOperationalHours)
                                Text(
                                    if(isFullDay) "Status: FULL DAY CLOSED"
                                    else "Closed: " + hours.sorted().joinToString(", ") { String.format("%02d:00", it) },
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { closureToDelete = date }) {
                                Icon(Icons.Default.Delete, "Remove", tint = Color.Gray)
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                Spacer(Modifier.height(16.dp))
                // =================================================================
                // --- 5. ACTION BUTTONS ---
                // =================================================================
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)) {

                    Button(
                        onClick = { showDeleteFacilityDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Delete")
                    }

                    Button(
                        onClick = {
                            selectedFacility?.id?.let { id ->
                                if (viewModel.formName.value.isBlank()) Toast.makeText(context, "Missing Name", Toast.LENGTH_SHORT).show()
                                else if (viewModel.formCategory.value.isBlank()) Toast.makeText(context, "Missing Category", Toast.LENGTH_SHORT).show()

                                else if (viewModel.formStartTime.value.isEmpty() || viewModel.formEndTime.value.isEmpty()) Toast.makeText(context, "Missing Hours", Toast.LENGTH_SHORT).show()
                                else if (start >= end) Toast.makeText(context, "Start time must be before End time", Toast.LENGTH_SHORT).show()
                                else {
                                    viewModel.updateFacility(
                                        docId = id,
                                        department = adminDepartment,
                                        dailyBreakHours = dailyBreakHours.toList().sorted(),
                                        specialClosures = specialClosuresMap.toMap(),
                                    ) { success, errorMsg ->
                                        if (success) {
                                            showSuccessDialog = true
                                        } else {
                                            Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            } ?: Toast.makeText(context, "No facility selected", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Submit")
                    }
                }
                Spacer(Modifier.height(32.dp))
            } else {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Select a facility from the dropdown above.", color = Color.Gray)
                }
            }
        }

        if (showDeleteFacilityDialog) {
            DeleteFacilityConfirmationDialog(
                facilityName = selectedFacility?.name ?: "this facility",
                onConfirmDelete = {
                    showDeleteFacilityDialog = false
                    selectedFacility?.id?.let { id ->
                        viewModel.deleteFacility(id) {
                            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                            selectedFacility = null
                            viewModel.clearForm()
                            specialClosuresMap.clear()
                            onNavigateBack()
                        }
                    }
                },
                onDismiss = { showDeleteFacilityDialog = false }
            )
        }

        closureToDelete?.let { dateRange ->
            DeleteClosureConfirmationDialog(
                dateRange = dateRange,
                onConfirmDelete = {
                    specialClosuresMap.remove(dateRange)
                    closureToDelete = null
                    Toast.makeText(context, "Closure Rule Deleted", Toast.LENGTH_SHORT).show()
                },
                onDismiss = { closureToDelete = null }
            )
        }
    }

    if (showSuccessDialog) {
        EditSuccessDialog(
            onOk = {
                showSuccessDialog = false
                onNavigateBack()
            },
            onDismiss = {
                showSuccessDialog = false
            }
        )
    }
}