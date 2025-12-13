package com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.myfacilitybookingsystem.viewModel.FacilityViewModel
import com.example.myfacilitybookingsystem.userInterface.adminTheme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddFacilityScreen(
    adminDepartment: String,
    onNavigateBack: () -> Unit,
    viewModel: FacilityViewModel = viewModel()
) {
    val context = LocalContext.current

    // Clear form on entry
    LaunchedEffect(Unit) { viewModel.clearForm() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Facility", color = Color.White, fontWeight = FontWeight.Bold) },
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

            LabeledInput("Department") { StaticInputText(text = adminDepartment) }
            Spacer(Modifier.height(16.dp))

            // Name
            LabeledInput("Facility Name") {
                TransparentTextField(viewModel.formName.value, { viewModel.formName.value = it }, placeholder = "e.g. Badminton Court 1")
            }
            Spacer(Modifier.height(16.dp))

            // Type
            LabeledInput("Type") {
                TransparentTextField(viewModel.formType.value, { viewModel.formType.value = it }, placeholder = "e.g. Court / Room / Lab")
            }
            Spacer(Modifier.height(16.dp))

            // Only show Capacity if NOT Sport
            if (adminDepartment != "Sport") {
                LabeledInput("Capacity (Pax)") {
                    TransparentTextField(
                        value = viewModel.formCapacity.value,
                        onValueChange = { if (it.all { char -> char.isDigit() }) viewModel.formCapacity.value = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        placeholder = "e.g. 4"
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            // Description
            LabeledInput("Description") {
                TransparentTextField(viewModel.formDesc.value, { viewModel.formDesc.value = it }, placeholder = "Details...")
            }
            Spacer(Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    if (viewModel.formName.value.isBlank()) {
                        Toast.makeText(context, "Name required", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addFacility(adminDepartment) {
                            Toast.makeText(context, "Facility Added!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Facility", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}