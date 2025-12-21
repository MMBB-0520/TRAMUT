package com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myfacilitybookingsystem.viewModel.AnnouncementViewModel
import com.example.myfacilitybookingsystem.userInterface.adminTheme.*

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAnnouncementScreen(
    announcementId: String,
    adminDepartment: String,
    viewModel: AnnouncementViewModel = viewModel(),
    onUpdateSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var isVenueExpanded by remember { mutableStateOf(false) }

    // 1. Load Data when screen opens
    LaunchedEffect(announcementId) {
        viewModel.loadAnnouncement(announcementId)
    }

    // Define options based on department
    val venueOptions = when (adminDepartment) {
        "Sport" -> listOf("All Sport Facilities", "Badminton Court", "Squash Court", "Gym")
        "Library" -> listOf("All Library Rooms", "Discussion Room", "Study Cube")
        else -> listOf("General", "Auditorium", "Meeting Room")
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Announcement", color = Color.White, fontWeight = FontWeight.Bold) },
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

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${uiState.error}", color = Color.Red)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Department (Read-Only)
                LabeledInput("Department") {
                    StaticInputText(text = adminDepartment)
                }
                Spacer(Modifier.height(16.dp))

                // Venue Dropdown
                LabeledInput("Venue Type") {
                    Box {
                        StaticInputText(
                            text = uiState.venueType.ifEmpty { "Select Venue" },
                            onClick = { isVenueExpanded = true },
                            icon = Icons.Default.ArrowDropDown
                        )
                        DropdownMenu(
                            expanded = isVenueExpanded,
                            onDismissRequest = { isVenueExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            venueOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        viewModel.onVenueChange(opt)
                                        isVenueExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Title Input
                LabeledInput("Title") {
                    TransparentTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChange(it) }
                    )
                }
                Spacer(Modifier.height(16.dp))

                // Description Input
                LabeledInput("Description") {
                    TransparentTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) }
                    )
                }
                Spacer(Modifier.height(32.dp))

                // Update Button
                Button(
                    onClick = {
                        // Passing 'docId' tells the ViewModel to UPDATE instead of CREATE
                        viewModel.saveAnnouncement(
                            docId = announcementId,
                            department = adminDepartment,
                            adminId = "" // Not needed for update
                        ) {
                            Toast.makeText(context, "Announcement Updated!", Toast.LENGTH_SHORT).show()
                            onUpdateSuccess()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Update", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}