package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility

import android.app.DatePickerDialog
import android.util.Log
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
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
import com.example.myfacilitybookingsystem.viewModel.FacilityViewModel
import com.example.tramut.userInterface.adminTheme.Facility.AddSuccessDialog
import com.example.tramut.userInterface.adminTheme.Facility.LabeledInput
import com.example.tramut.userInterface.adminTheme.Facility.LineStaticInput
import com.example.tramut.userInterface.studentTheme.LineTextField
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddFacilityScreen(
    adminDepartment: String,
    onNavigateBack: () -> Unit,
    viewModel: FacilityViewModel = viewModel()
) {
    val context = LocalContext.current
    var showSuccessDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isCategoryExpanded by remember { mutableStateOf(false) }
    var isStartTimeExpanded by remember { mutableStateOf(false) }
    var isEndTimeExpanded by remember { mutableStateOf(false) }
    var categoryWidth by remember { mutableStateOf(0) }
    var startTimeWidth by remember { mutableStateOf(0) }
    var endTimeWidth by remember { mutableStateOf(0) }
    fun getHour(timeStr: String): Int =
        if (timeStr.contains(":")) timeStr.split(":")[0].toIntOrNull() ?: 0 else 0
    val allTimeSlots = remember { (8..22).map { String.format("%02d:00", it) } }
    val availableEndTimes = remember(viewModel.formStartTime.value) {
        val startHour = getHour(viewModel.formStartTime.value)
        if (startHour == 0) allTimeSlots else allTimeSlots.filter { getHour(it) > startHour }
    }
    val dailyBreakHours = remember { mutableStateListOf<Int>() }
    val specialClosuresMap = remember { mutableStateMapOf<String, List<Int>>() }
    var isRangeMode by remember { mutableStateOf(false) }
    var closureSingleDate by remember { mutableStateOf("") }
    val closureSelectedHours = remember { mutableStateListOf<Int>() }
    var closureStartDate by remember { mutableStateOf("") }
    var closureEndDate by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()
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
    val venueOptions = remember(adminDepartment) {
        val baseOptions = when (adminDepartment) {
            "Sport Facilities" -> listOf(
                "Badminton","Squash","Gym","Guest/Karaoke Room","Swimming Pool","Snooker","Pickleball","Table Tennis","Tennis","Futsal"
            )
            "Library" -> listOf("Discussion Room","Discussion Room with PC","Individual Study Room"
            )
            "CITC" -> listOf("All Discussion Room", "Discussion Room (1 PC)","Discussion Room (2 PCs)", "Discussion Room with Projector (2 PCs)", "Discussion Room with Projector (2 PCs)[HDMI]"
            )
            else -> listOf("Error")
        }
        baseOptions + "Others"
    }
    var isCustomCategory by remember { mutableStateOf(false) }

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
            // ================= 1. BASIC INFO =================
            LabeledInput("Facility / Department") { LineStaticInput(text = adminDepartment) }
            Spacer(Modifier.height(16.dp))

            LabeledInput("Facility Name") {
                LineTextField(
                    viewModel.formName.value,
                    { viewModel.formName.value = it },
                    "e.g. Court 1"
                )
            }
            Spacer(Modifier.height(16.dp))
            LabeledInput("Category") {
                Column {
                    Box(modifier = Modifier.onSizeChanged { categoryWidth = it.width }) {
                        val displayText =
                            if (isCustomCategory) "Others" else viewModel.formCategory.value.ifEmpty { "Select Category" }
                        LineStaticInput(
                            text = displayText,
                            onClick = { isCategoryExpanded = true },
                            icon = Icons.Default.ArrowDropDown
                        )
                        DropdownMenu(
                            expanded = isCategoryExpanded,
                            onDismissRequest = { isCategoryExpanded = false },
                            modifier = Modifier.width(with(LocalDensity.current) { categoryWidth.toDp() })
                                .background(Color.White).heightIn(max = 250.dp)
                        ) {
                            venueOptions.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = {
                                    if (opt == "Others") {
                                        isCustomCategory = true; viewModel.formCategory.value = ""
                                    } else {
                                        isCustomCategory = false; viewModel.formCategory.value = opt
                                    }
                                    isCategoryExpanded = false
                                })
                            }
                        }
                    }
                    if (isCustomCategory) {
                        Spacer(Modifier.height(8.dp))
                        LineTextField(
                            viewModel.formCategory.value,
                            { viewModel.formCategory.value = it },
                            "Type custom category..."
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            if (adminDepartment != "Sport Facilities") {
                LabeledInput("Capacity (List, e.g., 4, 5, 6)") {
                    LineTextField(
                        viewModel.formCapacity.value,
                        { newValue ->
                            if (newValue.all { c -> c.isDigit() || c == ',' || c == ' ' }) {
                                viewModel.formCapacity.value = newValue
                            }
                        },
                        "Enter numbers separated by commas"
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ================= 2. OPERATING HOURS & DAILY BREAKS =================
            Text("Operating Hours (Daily Schedule)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Start Time
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
                                modifier = Modifier.width(with(LocalDensity.current) { startTimeWidth.toDp() })
                                    .background(Color.White).heightIn(max = 250.dp)
                            ) {
                                allTimeSlots.forEach { time ->
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
                                modifier = Modifier.width(with(LocalDensity.current) { endTimeWidth.toDp() })
                                    .background(Color.White).heightIn(max = 250.dp)
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
            if (viewModel.formStartTime.value.isNotEmpty() && viewModel.formEndTime.value.isNotEmpty()) {
                val start = getHour(viewModel.formStartTime.value)
                val end = getHour(viewModel.formEndTime.value)

                if (end > start) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Set Daily Break (Optional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Select hours to be CLOSED every day",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))

                    val operationalHours = (start until end).toList()
                    val columns = 4
                    val chunked = operationalHours.chunked(columns)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        chunked.forEach { rowHours ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowHours.forEach { hour ->
                                    val isBreak = dailyBreakHours.contains(hour)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                if (isBreak) Color.Red else Color.White,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .border(
                                                1.dp,
                                                if (isBreak) Color.Red else Color.LightGray,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .clickable {
                                                if (isBreak) dailyBreakHours.remove(hour) else dailyBreakHours.add(
                                                    hour
                                                )
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d:00", hour),
                                            fontSize = 12.sp,
                                            color = if (isBreak) Color.White else Color.Black,
                                            fontWeight = if (isBreak) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                                repeat(columns - rowHours.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // ================= 3. SPECIAL CLOSURES (DATE RANGES) =================
            Text("Special Closures (Exceptions)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFFEEEEF2), RoundedCornerShape(8.dp)).padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier.weight(1f).background(
                                if (!isRangeMode) Color.White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            ).clickable { isRangeMode = false }.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Specific Hours",
                                fontWeight = FontWeight.Bold,
                                color = if (!isRangeMode) Color.Black else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f).background(
                                if (isRangeMode) Color.White else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            ).clickable { isRangeMode = true }.padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Date Range (Full Day)",
                                fontWeight = FontWeight.Bold,
                                color = if (isRangeMode) Color.Black else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    if (!isRangeMode) {
                        LabeledInput("Select Date") {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { showDatePicker { closureSingleDate = it } }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DateRange, null, tint = Color.Gray)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    closureSingleDate.ifEmpty { "Pick a date" },
                                    fontSize = 16.sp,
                                    fontWeight = if (closureSingleDate.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                        HorizontalDivider()

                        if (closureSingleDate.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Select Hours to CLOSE (e.g. Competition):",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(Modifier.height(8.dp))

                            val hoursList = (8..22).toList()
                            val chunkedHours = hoursList.chunked(4)

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                chunkedHours.forEach { rowHours ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        rowHours.forEach { hour ->
                                            val isSelected = closureSelectedHours.contains(hour)
                                            Box(
                                                modifier = Modifier.weight(1f).background(
                                                    if (isSelected) Color.Red else Color(0xFFF0F0F0),
                                                    RoundedCornerShape(4.dp)
                                                ).clickable {
                                                    if (isSelected) closureSelectedHours.remove(hour) else closureSelectedHours.add(
                                                        hour
                                                    )
                                                }.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    String.format("%02d:00", hour),
                                                    fontSize = 12.sp,
                                                    color = if (isSelected) Color.White else Color.Black
                                                )
                                            }
                                        }
                                        repeat(4 - rowHours.size) { Spacer(Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "Close Facility for a period of time.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                LabeledInput("From") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                            .clickable { showDatePicker { closureStartDate = it } }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(closureStartDate.ifEmpty { "Start" }, fontSize = 14.sp)
                                    }
                                }
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                LabeledInput("To") {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                                            .clickable { showDatePicker { closureEndDate = it } }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.DateRange,
                                            null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(closureEndDate.ifEmpty { "End" }, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))


                    Button(
                        onClick = {
                            if (!isRangeMode) {
                                if (closureSingleDate.isNotEmpty() && closureSelectedHours.isNotEmpty()) {
                                    specialClosuresMap[closureSingleDate] =
                                        closureSelectedHours.toList()
                                    closureSelectedHours.clear(); closureSingleDate = ""
                                    Toast.makeText(
                                        context,
                                        "Added specific hours closure",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else Toast.makeText(
                                    context,
                                    "Select date & hours",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (closureStartDate.isNotEmpty() && closureEndDate.isNotEmpty()) {
                                    try {
                                        // Use Locale.US to ensure date format is consistent (yyyy-MM-dd)
                                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                        val start = sdf.parse(closureStartDate)
                                        val end = sdf.parse(closureEndDate)

                                        if (start != null && end != null && !start.after(end)) {
                                            val cal = Calendar.getInstance()
                                            cal.time = start

                                            while (!cal.time.after(end)) {
                                                val dateKey = sdf.format(cal.time)
                                                // Save the full range of operational hours (8 to 22)
                                                specialClosuresMap[dateKey] = (8..22).toList()
                                                cal.add(Calendar.DATE, 1)
                                            }

                                            // Reset fields
                                            closureStartDate = ""
                                            closureEndDate = ""
                                            Toast.makeText(context, "Range added to list", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AddFacility", "Error: ${e.message}")
                                    }
                                } else Toast.makeText(
                                    context,
                                    "Select Start & End dates",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp))
                        Text(if (isRangeMode) "Add Date Range Closure" else "Add Closure Exception")
                    }
                }
            }

            if (specialClosuresMap.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Scheduled Closures:", fontWeight = FontWeight.Bold)
                specialClosuresMap.toSortedMap().forEach { (date, hours) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .background(Color.White, RoundedCornerShape(4.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Date: $date", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val isFullDay = hours.containsAll((8..22).toList())
                            Text(
                                if (isFullDay) "Status: FULL DAY CLOSED" else "Closed: " + hours.sorted()
                                    .joinToString(", ") { String.format("%02d:00", it) },
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(onClick = { specialClosuresMap.remove(date) }) {
                            Icon(
                                Icons.Default.Delete,
                                "Remove",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // ================= 4. SUBMIT =================
            Button(
                onClick = {
                    val start = getHour(viewModel.formStartTime.value)
                    val end = getHour(viewModel.formEndTime.value)

                    if (viewModel.formName.value.isBlank()) Toast.makeText(
                        context,
                        "Missing Name",
                        Toast.LENGTH_SHORT
                    ).show()
                    else if (viewModel.formCategory.value.isBlank()) Toast.makeText(
                        context,
                        "Missing Category",
                        Toast.LENGTH_SHORT
                    ).show()
                    else if (viewModel.formStartTime.value.isEmpty() || viewModel.formEndTime.value.isEmpty()) Toast.makeText(
                        context,
                        "Missing Hours",
                        Toast.LENGTH_SHORT
                    ).show()
                    else if (start >= end) Toast.makeText(
                        context,
                        "Start time must be before End time",
                        Toast.LENGTH_SHORT
                    ).show()
                    else {
                        isLoading = true

                        viewModel.addFacility(
                            department = adminDepartment,
                            specialClosures = specialClosuresMap.toMap(),
                            dailyBreakHours = dailyBreakHours.toList().sorted()
                        ) { isSuccess, errorMessage ->
                            isLoading = false
                            if (isSuccess) showSuccessDialog = true
                            else Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                else Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(300.dp))
        }
    }

    if (showSuccessDialog) {
        AddSuccessDialog(
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

