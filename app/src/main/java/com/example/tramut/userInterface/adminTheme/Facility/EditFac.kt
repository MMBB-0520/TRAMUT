package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

    // Load data on entry
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
                                    viewModel.selectFacilityForEdit(facility)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            // 2. EDIT FORM (Only show if a facility is selected)
            if (selectedFacility != null) {

                LabeledInput("Facility Name") {
                    TransparentTextField(viewModel.formName.value, { viewModel.formName.value = it })
                }
                Spacer(Modifier.height(16.dp))

                LabeledInput("Status") {
                    Row {
                        FilterChip(selected = viewModel.formStatus.value == "Available", onClick = { viewModel.formStatus.value = "Available" }, label = { Text("Available") })
                        Spacer(Modifier.width(8.dp))
                        FilterChip(selected = viewModel.formStatus.value == "Maintenance", onClick = { viewModel.formStatus.value = "Maintenance" }, label = { Text("Maintenance") })
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (adminDepartment != "Sport") {
                    LabeledInput("Capacity") {
                        TransparentTextField(
                            value = viewModel.formCapacity.value,
                            onValueChange = { viewModel.formCapacity.value = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                Spacer(Modifier.height(16.dp))

                LabeledInput("Description") {
                    TransparentTextField(viewModel.formDesc.value, { viewModel.formDesc.value = it })
                }
                Spacer(Modifier.height(32.dp))

                // BUTTONS
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Delete Button
                    Button(
                        onClick = {
                            viewModel.deleteFacility(selectedFacility!!.id) {
                                Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show()
                                selectedFacility = null
                                viewModel.clearForm()
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
                            viewModel.updateFacility(selectedFacility!!.id, adminDepartment) {
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
                // Hint if nothing selected
                Text("Please select a facility from the dropdown above to start editing.", color = Color.Gray)
            }
        }
    }
}