package com.example.tramut.userInterface.studentTheme

import android.R.attr.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.PrimaryKey
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.ui.theme.Background
import com.google.firebase.firestore.DocumentId
import kotlin.String


@Composable
fun ReviewSubmissionScreen(
    containerColor: Color,
    bookings: List<Booking>,
    selectedBooking: Booking?,
    onBookingSelected: (Booking) -> Unit,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    comment: String,
    onCommentChange : (String) -> Unit,
    onSubmitReviewClick: () -> Unit,
    submitSuccess: Boolean,
    onOk: () -> Unit,
    onDismiss: () -> Unit
) {
    val canSubmit = selectedBooking != null &&
            !selectedCategory.isNullOrEmpty()


        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            BookingIdDropdown(
                bookings = bookings,
                selectedBooking = selectedBooking,
                onBookingSelected = onBookingSelected
            )

            InfoField(
                label = "Facility",
                value = selectedBooking?.facility ?: ""
            )

            InfoField(
                label = "Date",
                value = selectedBooking?.date ?: ""
            )

            InfoField(
                label = "Venue / Room No.",
                value = selectedBooking?.venue ?: ""
            )

            Spacer(modifier = Modifier.height(12.dp))
            IssueCategoryDropdown(
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )


            Spacer(modifier = Modifier.height(4.dp))


            Divider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Description",
                fontSize = 12.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                placeholder = {
                    Text(
                        text = "Description",
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .height(180.dp),
                shape = RoundedCornerShape(6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onSubmitReviewClick,
                enabled = canSubmit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor
                )
            ) {
                Text(
                    text = "SUBMIT",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (submitSuccess) {
                SuccessDialogShared(
                    contentDescription = "Review Submitted",
                    onOk = onOk,
                    onDismiss = onDismiss
                )
            }
        }
}
@Composable
fun InfoField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Divider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
@Composable
fun BookingIdDropdown(
    bookings: List<Booking>,
    selectedBooking: Booking?,
    onBookingSelected: (Booking) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {

        Row {
            Text(
                text = "Booking ID",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = " *",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = selectedBooking?.bookingId ?: "Select Booking",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = if (expanded)
                        Icons.Filled.KeyboardArrowUp
                    else
                        Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                bookings.forEach { booking ->
                    DropdownMenuItem(
                        text = { Text(booking.bookingId) },
                        onClick = {
                            onBookingSelected(booking)
                            expanded = false
                        }
                    )
                }
            }
        }

        Divider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
@Composable
fun IssueCategoryDropdown(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "All Issues",
        "Damage/Broken Items",
        "Network/Technology Issues",
        "Plumbing/Ventilation Issues",
        "Electrical/Lighting Issues",
        "Cleanliness & Safety",
        "Other"
    )

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row {
            Text(
                text = "Issue Category",
                fontSize = 12.sp,
                color = Color.DarkGray
            )
            Text(
                text = " *",
                color = Color.Red,
                fontSize = 12.sp
            )
        }

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .background(MaterialTheme.colorScheme.background)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedCategory ?: "Select Category",
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded)
                        Icons.Filled.KeyboardArrowUp
                    else
                        Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach {
                    DropdownMenuItem(
                        text = { Text(it) },
                        onClick = {
                            onCategorySelected(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
