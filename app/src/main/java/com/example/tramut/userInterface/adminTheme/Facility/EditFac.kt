package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.rooms.entity.Facility
import com.example.myfacilitybookingsystem.viewModel.FacilityViewModel
import com.example.myfacilitybookingsystem.userInterface.adminTheme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditFacilityScreen(
    adminDepartment: String,
    onNavigateBack: () -> Unit,
    viewModel: FacilityViewModel = viewModel()
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var selectedFacility by remember { mutableStateOf<Facility?>(null) }

    // --- STATE FOR SPECIAL CLOSURES ---
    val specialClosuresMap = remember { mutableStateMapOf<String, List<Int>>() }
    var selectedDateForClosure by remember { mutableStateOf("") }
    val selectedHoursForClosure = remember { mutableStateListOf<Int>() }

    // Time slots for dropdown
    val timeSlots = remember { (8..22).map { String.format("%02d:00", it) } }

    // Calendar Setup
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

    // Load facilities when screen opens
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
        containerColor = Color(0xFFF5F5F5) // Light Grey Background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            // --- 1. SELECTION CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Select Facility", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))

                    Box {
                        OutlinedTextField(
                            value = selectedFacility?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Tap to choose...") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true }, // Make the whole box clickable
                            enabled = false, // Disable typing
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledPlaceholderColor = Color.Gray,
                                disabledTrailingIconColor = Color.Black
                            )
                        )
                        // Invisible overlay to capture click
                        Box(
                            Modifier
                                .matchParentSize()
                                .clickable { expanded = true }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .fillMaxWidth(0.85f)
                                .heightIn(max = 300.dp)
                        ) {
                            viewModel.facilityList.forEach { facility ->
                                DropdownMenuItem(
                                    text = { Text(facility.name) },
                                    onClick = {
                                        try {
                                            selectedFacility = facility
                                            viewModel.selectFacilityForEdit(facility)

                                            // Safety Reset
                                            specialClosuresMap.clear()
                                            if (facility.specialClosures != null) {
                                                specialClosuresMap.putAll(facility.specialClosures)
                                            }
                                            expanded = false
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error selecting facility", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 2. EDIT FORM CARD ---
            if (selectedFacility != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("Facility Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.height(16.dp))

                        // Name
                        OutlinedTextField(
                            value = viewModel.formName.value,
                            onValueChange = { viewModel.formName.value = it },
                            label = { Text("Facility Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(Modifier.height(16.dp))

                        // Status Chips
                        Text("Status", fontSize = 14.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row {
                            FilterChip(
                                selected = viewModel.formStatus.value == "Available",
                                onClick = { viewModel.formStatus.value = "Available" },
                                label = { Text("Available") },
                                leadingIcon = { if(viewModel.formStatus.value == "Available") Icon(Icons.Default.Add, null) }
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = viewModel.formStatus.value == "Maintenance",
                                onClick = { viewModel.formStatus.value = "Maintenance" },
                                label = { Text("Maintenance") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFEBEE),
                                    selectedLabelColor = Color.Red
                                )
                            )
                        }
                        Spacer(Modifier.height(16.dp))

                        // Capacity
                        if (adminDepartment != "Sport") {
                            OutlinedTextField(
                                value = viewModel.formCapacity.value,
                                onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.formCapacity.value = it },
                                label = { Text("Capacity") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                        }

                        // Hours
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
                    }
                }

                Spacer(Modifier.height(16.dp))

                // --- 3. MAINTENANCE EXCEPTIONS CARD ---
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Special Maintenance", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Close specific hours on specific dates.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))

                        // Date Picker Button
                        OutlinedButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.DateRange, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (selectedDateForClosure.isEmpty()) "Select Date" else selectedDateForClosure)
                        }

                        // Hour Selection Grid
                        if (selectedDateForClosure.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("Tap hours to close:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))

                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                (8..22).forEach { hour ->
                                    val isSelected = selectedHoursForClosure.contains(hour)
                                    Box(
                                        modifier = Modifier
                                            .width(70.dp)
                                            .background(
                                                if (isSelected) Color.Red else Color(0xFFEEEEEE),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .border(1.dp, if(isSelected) Color.Red else Color.Gray, RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isSelected) selectedHoursForClosure.remove(hour)
                                                else selectedHoursForClosure.add(hour)
                                            }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = String.format("%02d:00", hour),
                                            color = if (isSelected) Color.White else Color.Black,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
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
                                        Toast.makeText(context, "Exception Added", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Add Closure Rule")
                            }
                        }

                        // List of Existing Closures
                        if (specialClosuresMap.isNotEmpty()) {
                            HorizontalDivider(Modifier.padding(vertical = 16.dp))
                            Text("Active Closures:", fontWeight = FontWeight.Bold)
                            specialClosuresMap.forEach { (date, hours) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            "Closed: ${hours.sorted().joinToString(", ") { "$it:00" }}",
                                            color = Color.Red, fontSize = 12.sp
                                        )
                                    }
                                    IconButton(onClick = { specialClosuresMap.remove(date) }) {
                                        Icon(Icons.Default.Delete, "Delete", tint = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- 4. ACTION BUTTONS ---
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // DELETE BUTTON
                    Button(
                        onClick = {
                            selectedFacility?.id?.let { id ->
                                viewModel.deleteFacility(id) {
                                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                                    selectedFacility = null
                                    viewModel.clearForm()
                                    specialClosuresMap.clear()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Delete")
                    }

                    // UPDATE BUTTON
                    Button(
                        onClick = {
                            selectedFacility?.id?.let { id ->
                                viewModel.updateFacility(
                                    docId = id,
                                    department = adminDepartment,
                                    specialClosures = specialClosuresMap.toMap()
                                ) { success, errorMsg -> // *** FIXED: Using callback properly
                                    if (success) {
                                        Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                                        onNavigateBack()
                                    } else {
                                        Toast.makeText(context, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Save Changes")
                    }
                }
                Spacer(Modifier.height(50.dp))
            } else {
                // Empty state when no facility selected
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Select a facility from the dropdown above.", color = Color.Gray)
                }
            }
        }
    }
}

// Reusable Time Selector
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

        Box {
            OutlinedTextField(
                value = selectedTime,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color.Gray
                )
            )
            Box(Modifier.matchParentSize().clickable { expanded = true })

            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                timeOptions.forEach { time ->
                    DropdownMenuItem(text = { Text(time) }, onClick = { onTimeSelected(time); expanded = false })
                }
            }
        }
    }
}