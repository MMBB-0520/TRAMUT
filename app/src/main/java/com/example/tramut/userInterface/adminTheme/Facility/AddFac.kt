package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility


import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.userInterface.adminTheme.LabeledInput
import com.example.myfacilitybookingsystem.userInterface.adminTheme.TransparentTextField
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

    // --- STATE ---
    val timeSlots = remember { (8..22).map { String.format("%02d:00", it) } }
    val specialClosuresMap = remember { mutableStateMapOf<String, List<Int>>() }
    var selectedDateForClosure by remember { mutableStateOf("") }
    val selectedHoursForClosure = remember { mutableStateListOf<Int>() }

    // Calendar Dialog Setup
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
            LabeledInput("Facility Name") {
                TransparentTextField(
                    viewModel.formName.value,
                    { viewModel.formName.value = it },
                    placeholder = "e.g. Meeting Room A"
                )
            }
            Spacer(Modifier.height(16.dp))

            // ==========================================
            // START: NEW CAPACITY SECTION
            // ==========================================
            if (adminDepartment != "Sport") {
                LabeledInput("Capacity (Pax)") {
                    TransparentTextField(
                        value = viewModel.formCapacity.value,
                        onValueChange = { input ->
                            // Only allow numbers
                            if (input.all { it.isDigit() }) viewModel.formCapacity.value = input
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        placeholder = "e.g. 50"
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            // ==========================================
            // END: NEW CAPACITY SECTION
            // ==========================================

            // --- SECTION 2: DEFAULT HOURS ---
            Text("Default Operating Hours", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("This applies to every day unless specified below.", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(Modifier.weight(1f)) {
                    TimeDropdownSelector("Opens At", viewModel.formStartTime.value, timeSlots) {
                        viewModel.formStartTime.value = it
                    }
                }
                Box(Modifier.weight(1f)) {
                    TimeDropdownSelector("Closes At", viewModel.formEndTime.value, timeSlots) {
                        viewModel.formEndTime.value = it
                    }
                }
            }
            Spacer(Modifier.height(24.dp))

            // --- SECTION 3: SPECIAL CLOSURES ---
            Text("Special Closures / Maintenance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Select a specific date to close specific hours.", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(12.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // A. Pick Date Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() }
                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, null, tint = Color.Gray)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (selectedDateForClosure.isEmpty()) "Select Date (e.g., for Maintenance)" else selectedDateForClosure,
                            fontWeight = if (selectedDateForClosure.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    if (selectedDateForClosure.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text("Select Hours to CLOSE on this date:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))

                        // B. Multi-Select Grid for Hours
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 60.dp),
                            modifier = Modifier.height(150.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items((8..22).toList()) { hour ->
                                val isSelected = selectedHoursForClosure.contains(hour)
                                Box(
                                    modifier = Modifier
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
                        }

                        // C. Add Button
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
                            Text("Add Exception for Date")
                        }
                    }
                }
            }

            // --- SECTION 4: LIST OF ADDED EXCEPTIONS ---
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

            // --- SAVE BUTTON ---
            Button(
                onClick = {
                    if (viewModel.formName.value.isBlank()) {
                        // Error handling
                    } else {
                        viewModel.addFacility(
                            department = adminDepartment,
                            specialClosures = specialClosuresMap.toMap()
                        ) {
                            showSuccessDialog = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Facility Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(50.dp))
        }
    }

    // Success Dialog (Same as before)
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
                    Icon(painter = painterResource(com.example.tramut.R.drawable.correct), null, tint = Color.Unspecified, modifier = Modifier.size(50.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Facility Added Successfully!", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun TimeDropdownSelector(
    label: String,
    selectedTime: String,
    timeOptions: List<String>,
    onTimeSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTime.ifEmpty { "Select" },
                    color = if (selectedTime.isEmpty()) Color.Gray else Color.Black
                )
                Icon(Icons.Default.ArrowDropDown, "Select")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time) },
                        onClick = {
                            onTimeSelected(time)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}