package com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement

import androidx.compose.ui.unit.sp

import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import com.example.myfacilitybookingsystem.viewModel.AnnouncementViewModel
import com.example.myfacilitybookingsystem.userInterface.adminTheme.* // For LabeledInput, TransparentTextField, etc.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostAnnouncementScreen(
    adminDepartment: String,
    adminId: String,
    onNavigateBack: () -> Unit,
    viewModel: AnnouncementViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isVenueExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.resetState() }

    val venueOptions = when (adminDepartment) {
        "Sport" -> listOf("All Sport Facilities", "Badminton Court", "Squash Court", "Gym")
        "Library" -> listOf("All Library Rooms", "Discussion Room", "Study Cube")
        else -> listOf("General", "Auditorium", "Meeting Room")
    }

    fun showDatePicker(onDateSelected: (String) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(context, { _, y, m, d -> onDateSelected("$d/${m + 1}/$y") }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Post Announcement", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) } }
            )
        },
        containerColor = Color(0xFFEEEEF2)
    ) { padding ->
        Column(Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(24.dp)) {
            LabeledInput("Facility / Department") { StaticInputText(text = adminDepartment) }
            Spacer(Modifier.height(16.dp))

            LabeledInput("Venue Type") {
                Box {
                    StaticInputText(uiState.venueType.ifEmpty { "Select Venue" }, { isVenueExpanded = true }, Icons.Default.ArrowDropDown)
                    DropdownMenu(isVenueExpanded, { isVenueExpanded = false }, Modifier.background(Color.White)) {
                        venueOptions.forEach { opt -> DropdownMenuItem({ Text(opt) }, { viewModel.onVenueChange(opt); isVenueExpanded = false }) }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            LabeledInput("Title") { TransparentTextField(uiState.title, { viewModel.onTitleChange(it) }, placeholder = "Title") }
            Spacer(Modifier.height(16.dp))

            Text("Date Range", fontWeight = FontWeight.Bold)
            Row {
                Box(Modifier.weight(1f)) { LabeledInput("Start") { StaticInputText(uiState.startDate.ifEmpty { "DD/MM/YYYY" }, { showDatePicker { viewModel.onStartDateChange(it) } }, Icons.Default.DateRange) } }
                Spacer(Modifier.width(16.dp))
                Box(Modifier.weight(1f)) { LabeledInput("End") { StaticInputText(uiState.endDate.ifEmpty { "DD/MM/YYYY" }, { showDatePicker { viewModel.onEndDateChange(it) } }, Icons.Default.DateRange) } }
            }
            Spacer(Modifier.height(16.dp))

            LabeledInput("Description") { TransparentTextField(uiState.description, { viewModel.onDescriptionChange(it) }, placeholder = "Details...") }
            Spacer(Modifier.height(32.dp))

            Button(
                onClick = {
                    if (uiState.title.isEmpty()) Toast.makeText(context, "Title required", Toast.LENGTH_SHORT).show()
                    else viewModel.saveAnnouncement(null, adminDepartment, adminId) { Toast.makeText(context, "Posted!", Toast.LENGTH_SHORT).show(); onNavigateBack() }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) CircularProgressIndicator(color = Color.White) else Text("Submit", fontSize = 16.sp)
            }
        }
    }
}