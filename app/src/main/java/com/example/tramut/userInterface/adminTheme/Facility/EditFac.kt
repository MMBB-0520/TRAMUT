package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

@OptIn(ExperimentalMaterial3Api::class)
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
    LaunchedEffect(adminDepartment) { viewModel.loadFacilities(adminDepartment) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Facility", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } }
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

            // 1. DROPDOWN TO SELECT FACILITY
            LabeledInput("Select Facility to Edit") {
                Box {
                    StaticInputText(
                        text = selectedFacility?.name ?: "Tap to choose...",
                        onClick = { expanded = true },
                        icon = Icons.Default.ArrowDropDown
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White).fillMaxWidth(0.9f)
                    ) {
                        viewModel.facilityList.forEach { facility ->
                            DropdownMenuItem(
                                text = { Text(facility.name) },
                                onClick = {
                                    selectedFacility = facility
                                    viewModel.selectFacilityForEdit(facility) // Pre-fill form text fields

                                    // *** CRITICAL: LOAD EXISTING CLOSURES INTO THE MAP ***
                                    specialClosuresMap.clear()
                                    specialClosuresMap.putAll(facility.specialClosures)

                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            // 2. EDIT FORM (Only show if a facility is selected)
            if (selectedFacility != null) {

                // --- NAME ---
                LabeledInput("Facility Name") {
                    TransparentTextField(viewModel.formName.value, { viewModel.formName.value = it })
                }
                Spacer(Modifier.height(16.dp))

                // --- STATUS ---
                LabeledInput("Status") {
                    Row {
                        FilterChip(
                            selected = viewModel.formStatus.value == "Available",
                            onClick = { viewModel.formStatus.value = "Available" },
                            label = { Text("Available") }
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = viewModel.formStatus.value == "Maintenance",
                            onClick = { viewModel.formStatus.value = "Maintenance" },
                            label = { Text("Maintenance") },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF44336), selectedLabelColor = Color.White)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // --- CAPACITY (Added as requested) ---
                if (adminDepartment != "Sport") {
                    LabeledInput("Capacity (Pax)") {
                        TransparentTextField(
                            value = viewModel.formCapacity.value,
                            onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.formCapacity.value = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            placeholder = "e.g. 50"
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // --- OPERATING HOURS ---
                Text("Default Operating Hours", fontWeight = FontWeight.Bold, fontSize = 14.sp)
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

                // --- SPECIAL CLOSURES / MAINTENANCE (New Section) ---
                Text("Special Closures / Maintenance", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Select a specific date to close specific hours.", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Date Picker
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
                                text = if (selectedDateForClosure.isEmpty()) "Select Date" else selectedDateForClosure,
                                fontWeight = if (selectedDateForClosure.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (selectedDateForClosure.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("Select Hours to CLOSE:", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(8.dp))

                            // Grid Selection
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

                            // Add Button
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

                // List of Added Exceptions
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
                                Text(text = date, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Closed: " + hours.sorted().joinToString(", ") { String.format("%02d:00", it) },
                                    color = Color.Red, fontSize = 12.sp
                                )
                            }
                            IconButton(onClick = { specialClosuresMap.remove(date) }) {
                                Icon(Icons.Default.Delete, "Remove", tint = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // --- BUTTONS (Delete & Update) ---
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Delete Button
                    Button(
                        onClick = {
                            viewModel.deleteFacility(selectedFacility!!.id) {
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                                selectedFacility = null
                                viewModel.clearForm()
                                specialClosuresMap.clear()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }

                    // Update Button
                    Button(
                        onClick = {
                            // Update the logic to include the map
                            viewModel.updateFacility(
                                docId = selectedFacility!!.id,
                                department = adminDepartment,
                                specialClosures = specialClosuresMap.toMap() // Pass the map
                            ) {
                                Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(50.dp)
                    ) {
                        Text("Update", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Select a facility above to start editing.", color = Color.Gray)
                }
            }
        }
    }
}