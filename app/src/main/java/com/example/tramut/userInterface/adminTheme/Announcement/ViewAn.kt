package com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.example.myfacilitybookingsystem.rooms.repo.AnnouncementRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementScreen(
    currentAdminDepartment: String,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { AnnouncementRepository() }
    val announcements = remember { mutableStateListOf<Announcement>() }
    var isLoading by remember { mutableStateOf(true) }

    // States for swipe-to-delete confirmation
    var announcementIdToDelete by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }


    LaunchedEffect(currentAdminDepartment) {
        repository.getAdminAnnouncementsFlow(currentAdminDepartment).collect { list ->
            announcements.clear()
            announcements.addAll(list)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Announcements", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd, containerColor = Color.Black, contentColor = Color.White) {
                Icon(Icons.Default.Add, "Add")
            }
        },
        containerColor = Color(0xFFEEEEF2)
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (announcements.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No announcements found.", color = Color.Gray) }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = announcements, key = { it.id }) { item ->
                    SwipeableAnnouncementItem(
                        item = item,
                        onClick = { onNavigateToDetail(item.id) },
                        onEdit = { onNavigateToEdit(item.id) },
                        onDelete = {
                            // 1. Store the ID of the item to be deleted
                            announcementIdToDelete = item.id
                            // 2. Show the confirmation dialog
                            showDeleteConfirmationDialog = true
                        }
                    )
                }

                // Removed the manual "DELETE ANNOUNCEMENT" button as requested
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // --- Delete Confirmation Dialog (Triggered by Swipe) ---
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmationDialog = false
                announcementIdToDelete = null // Clear ID if user dismisses/taps outside
            },
            title = { Text("Delete Confirmation") },
            text = { Text("Are you sure you want to delete this announcement? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        announcementIdToDelete?.let { id ->
                            scope.launch {
                                // Perform the actual deletion
                                repository.deleteAnnouncement(id)
                                showDeleteConfirmationDialog = false
                                announcementIdToDelete = null
                                // Show success message
                                showDeleteSuccessDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE57373))
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // When user clicks 'Cancel':
                        showDeleteConfirmationDialog = false
                        announcementIdToDelete = null // Clear ID
                        // The SwipeableAnnouncementItem's LaunchedEffect handles the snap-back
                    }
                ) { Text("Cancel") }
            }
        )
    }

    // --- Delete Success Dialog ---
    if (showDeleteSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSuccessDialog = false },
            title = {
                Text(
                    "Deleted successfully",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    // FIX: Use fillMaxWidth() and textAlign to center the text
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDeleteSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("OK", color = Color.White) }
            }
        )
    }
}