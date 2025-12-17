package com.example.tramut.userInterface.adminTheme

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.rooms.entity.Review
import com.example.tramut.viewModel.ReviewViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewScreen(
    adminDepartment: String,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val categoryOptions = listOf("All Issues", "Damage/Broken Items", "Network/Technology Issues", "Plumbing/Ventilation Issues", "Electrical/Lighting Issues", "Cleanliness & Safety", "Other")
    val statusOptions = listOf("All Status", "Unsolved", "Pending", "Solved")

    var selectedCategory by remember { mutableStateOf(categoryOptions.first()) }
    var selectedStatus by remember { mutableStateOf(statusOptions.first()) }

    var selectedReview by remember { mutableStateOf<Review?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    val reviews = remember { mutableStateListOf<Review>() }

    // Firebase Listener
    LaunchedEffect(adminDepartment) {
        val db = Firebase.firestore
        db.collection("reviews")
            .whereEqualTo("department", adminDepartment)
            .addSnapshotListener { value, _ ->
                if (value != null) {
                    reviews.clear()
                    reviews.addAll(value.toObjects(Review::class.java))
                }
            }
    }

    // --- FILTERING LOGIC ---
    val filteredReviews = reviews.filter { review ->
        val matchesCategory = selectedCategory == "All Issues" || review.issueCategory == selectedCategory
        val matchesStatus = selectedStatus == "All Status" || review.status == selectedStatus
        matchesCategory && matchesStatus
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Maintenance Reviews", color = Color.White, fontWeight = FontWeight.Bold) },
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
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {

            // --- LINE-STYLE DROPDOWN FILTERS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.weight(1.2f)) {
                    LineDropdownField("Issue Type", selectedCategory, categoryOptions) { selectedCategory = it }
                }
                Box(modifier = Modifier.weight(0.8f)) {
                    LineDropdownField("Status", selectedStatus, statusOptions) { selectedStatus = it }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Display "No Reviews" if list is empty
            if (filteredReviews.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No reviews found for this selection.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredReviews) { review ->
                        ReviewItemCard(
                            review = review,
                            onClick = {
                                selectedReview = review
                                showSheet = true
                            }
                        )
                    }
                }
            }
        }

        if (showSheet && selectedReview != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                ReviewDetailContent(
                    review = selectedReview!!,
                    onStatusUpdate = { newStatus ->
                        viewModel.updateStatus(selectedReview!!.id, newStatus) {
                            showSheet = false
                            Toast.makeText(context, "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LineDropdownField(
    label: String,
    currentSelection: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var width by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { width = with(density) { it.size.width.toDp() } }
                .clickable { expanded = true }
                .padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currentSelection,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
            }
            // The Underline
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomStart),
                thickness = 1.dp,
                color = Color.LightGray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(width).background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 14.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper: Status Badge remains the same as your provided design
@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status) {
        "Solved" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "Pending" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        else -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }
    Surface(color = bgColor, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// Card and Detail Content updated to clean line logic
@Composable
fun ReviewItemCard(review: Review, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatusBadge(status = review.status)
                Text(review.bookingDate, color = Color.Gray, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(review.venue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(review.issueCategory, color = Color(0xFFD32F2F), fontSize = 14.sp)
            HorizontalDivider(Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
            Text("Feedback from ${review.userName}:", fontSize = 11.sp, color = Color.Gray)
            Text(review.comment, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun ReviewDetailContent(review: Review, onStatusUpdate: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(24.dp).padding(bottom = 20.dp)) {
        Text("Issue Details", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(16.dp))

        // Simple lines instead of boxes for details
        DetailLine("Venue", review.venue)
        DetailLine("Category", review.issueCategory)
        DetailLine("Reported By", review.userName)

        Spacer(Modifier.height(16.dp))
        Text("COMMENT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(review.comment, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onStatusUpdate("Pending") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Pending", color = Color.Black) }
            Button(
                onClick = { onStatusUpdate("Solved") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp)
            ) { Text("Solved") }
        }
    }
}

@Composable
fun DetailLine(label: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        HorizontalDivider(Modifier.padding(top = 4.dp), thickness = 0.5.dp, color = Color.LightGray)
    }
}