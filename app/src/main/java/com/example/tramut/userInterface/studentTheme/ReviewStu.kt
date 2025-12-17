package com.example.tramut.userInterface.userTheme

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.userInterface.adminTheme.Facility.AddSuccessDialog
import com.example.tramut.viewModel.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAddReviewScreen(
    booking: Booking,
    onNavigateBack: () -> Unit,
    viewModel: ReviewViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val StudentBlue = Color(0xFF0C1DBC)

    var selectedCategory by remember { mutableStateOf("") }
    var otherDetail by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    val issueCategories = listOf("Damage/Broken Items", "Network/Technology Issues", "Plumbing/Ventilation Issues", "Electrical/Lighting Issues", "Cleanliness & Safety", "Other")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Submit Review", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = StudentBlue),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFFEEEEF2)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp) // Spacing handled inside components
        ) {
            Text("BOOKING INFORMATION", fontWeight = FontWeight.ExtraBold, color = StudentBlue, fontSize = 12.sp)

            // --- STATIC FIELDS (Fixed Data) ---
            LabeledLineSection("Facility / Department") {
                LineStaticInput(text = booking.venueType)
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    LabeledLineSection("Venue") { LineStaticInput(text = booking.venue) }
                }
                Spacer(Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    LabeledLineSection("Date") { LineStaticInput(text = booking.date) }
                }
            }

            LabeledLineSection("Booking No") {
                LineStaticInput(text = booking.bookingNo)
            }

            Spacer(Modifier.height(16.dp))
            Text("ISSUE DETAILS", fontWeight = FontWeight.ExtraBold, color = StudentBlue, fontSize = 12.sp)

            // --- INPUT FIELDS (Dropdown & Text) ---
            LabeledLineSection("Issue Category") {
                Box {
                    LineStaticInput(
                        text = selectedCategory.ifEmpty { "Select Category" },
                        onClick = { isExpanded = true },
                        icon = Icons.Default.ArrowDropDown
                    )
                    DropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(Color.White)
                    ) {
                        issueCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    isExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedCategory == "Other") {
                LabeledLineSection("Specify Other Issue") {
                    LineEditableInput(
                        value = otherDetail,
                        onValueChange = { otherDetail = it },
                        placeholder = "What is the issue?"
                    )
                }
            }

            LabeledLineSection("Description") {
                LineEditableInput(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Tell us more about the problem..."
                )
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = {
                    if (selectedCategory.isNotEmpty() && description.isNotEmpty()) {
                        viewModel.submitReview(booking, selectedCategory, otherDetail, description, context)
                    } else {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StudentBlue),
                shape = RoundedCornerShape(12.dp),
                enabled = !viewModel.isSaving
            ) {
                if (viewModel.isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Submit Report", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    if (viewModel.showSuccessDialog) {
        AddSuccessDialog(onOk = { viewModel.dismissSuccess(); onNavigateBack() }, onDismiss = { viewModel.dismissSuccess() })
    }
}

// --- HELPER COMPONENTS BASED ON YOUR DESIGN ---

@Composable
fun LabeledLineSection(label: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
fun LineStaticInput(text: String, onClick: () -> Unit = {}, icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    Box(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(top = 8.dp, bottom = 8.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = text, fontSize = 16.sp, color = if (text.startsWith("Select") || text.startsWith("Pick")) Color.Gray else Color.Black)
                if (icon != null) Icon(icon, null, tint = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        }
    }
}

@Composable
fun LineEditableInput(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) Text(placeholder, color = Color.Gray, fontSize = 16.sp)
                innerTextField()
            }
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
    }
}