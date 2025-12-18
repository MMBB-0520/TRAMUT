package com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.example.myfacilitybookingsystem.userInterface.adminTheme.BorderGray
import com.example.myfacilitybookingsystem.userInterface.adminTheme.CheckGreen
import com.example.myfacilitybookingsystem.viewModel.AnnouncementViewModel
import com.example.tramut.ui.theme.ErrorRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableAnnouncementItem(
    item: Announcement,
    isConfirming : Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope() // Required to call reset()
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.StartToEnd -> { onEdit(); false }
                SwipeToDismissBoxValue.EndToStart -> {
                    isRemoved = true
                    true
                }
                else -> false
            }
        }
    )

    // FIX: Listen for when the dialog is closed without deleting
    LaunchedEffect(isConfirming) {
        // If the parent says we are no longer confirming, and the item is currently hidden
        if (!isConfirming && isRemoved) {
            isRemoved = false // Make it visible again
            dismissState.reset() // Slide the card back to the center
        }
    }

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(500)
            onDelete()
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically() + fadeOut(),
        enter = expandVertically() + fadeIn()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) CheckGreen else ErrorRed
                val align = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                val icon = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Icons.Default.Edit else Icons.Default.Delete
                Box(Modifier.fillMaxSize().background(color, RoundedCornerShape(8.dp)).padding(horizontal = 20.dp), contentAlignment = align) {
                    Icon(icon, null, tint = Color.White)
                }
            },
            content = { AnnouncementCard(item, onClick) }
        )
    }
}


@Composable
fun AnnouncementCard(item: Announcement, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderGray),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.venue, color = Color.Gray, fontSize = 12.sp)
                Text(item.created_date_str, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AnnouncementDetailDialog(announcement: Announcement, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(announcement.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Close") }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Venue: ${announcement.venue}", fontWeight = FontWeight.SemiBold)
                Text("Posted by: ${announcement.admin_id} (${announcement.department})", color = Color.Gray, fontSize = 12.sp)
                Text("Date: ${announcement.created_date_str} - ${announcement.expiry_date_str}", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(announcement.content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementDetailScreen(
    announcementId: String,
    viewModel: AnnouncementViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(announcementId) {
        viewModel.loadAnnouncement(announcementId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Announcement Detail", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },

        containerColor = Color(0xFFEEEEF2) // Light gray background
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. Loading State
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Black
                )
            }
            // 2. Error State
            else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error loading announcement", fontWeight = FontWeight.Bold)
                    Text(uiState.error ?: "", color = Color.Gray)
                }
            }
            // 3. Success State (Content)
            else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Title
                    Text(
                        text = uiState.title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        DetailChip(icon = Icons.Default.DateRange, text = uiState.startDate)
                        if (uiState.venueType.isNotEmpty()) {
                            DetailChip(icon = Icons.Default.LocationOn, text = uiState.venueType)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(24.dp))

                    // Main Content
                    Text(
                        text = uiState.description,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun DetailChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
