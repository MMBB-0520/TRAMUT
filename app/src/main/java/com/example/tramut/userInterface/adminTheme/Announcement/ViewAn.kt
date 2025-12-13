package com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.example.myfacilitybookingsystem.rooms.repo.AnnouncementRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementScreen(
    currentAdminDepartment: String,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { AnnouncementRepository() }
    val announcements = remember { mutableStateListOf<Announcement>() }
    var isLoading by remember { mutableStateOf(true) }
    var selectedAnnouncement by remember { mutableStateOf<Announcement?>(null) }

    LaunchedEffect(currentAdminDepartment) {
        repository.getAnnouncementsFlow(currentAdminDepartment).collect { list ->
            announcements.clear()
            announcements.addAll(list)
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("$currentAdminDepartment Announcements", color = Color.White, fontWeight = FontWeight.Bold) },
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
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = announcements, key = { it.id }) { item ->
                    SwipeableAnnouncementItem(
                        item = item,
                        onClick = { selectedAnnouncement = item },
                        onEdit = { onNavigateToEdit(item.id) },
                        onDelete = { scope.launch { repository.deleteAnnouncement(item.id) } }
                    )
                }
            }
        }
        if (selectedAnnouncement != null) {
            AnnouncementDetailDialog(announcement = selectedAnnouncement!!, onDismiss = { selectedAnnouncement = null })
        }
    }
}