package com.example.tramut.userInterface.studentTheme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tramut.AppScreen
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.viewModel.MyBookingViewModel

enum class BookingTab {
    UPCOMING, PENDING, CANCELLED, ALL
}

@Composable
fun MyBookingScreen(
    navController: NavController,
    viewModel: MyBookingViewModel = viewModel(),
    userId: String
) {
    Log.d("MyBookingScreen", "User ID received: $userId")

    if (userId.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val bookingList by viewModel.bookingList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(BookingTab.ALL) }

    // 启动 Firestore 监听
    LaunchedEffect(userId) {
        viewModel.startListening(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // 标签页选择器 - 没有标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BookingTab.values().forEach { tab ->
                BookingTabButton(
                    tab = tab,
                    isSelected = selectedTab == tab,
                    onClick = { selectedTab = tab }
                )
            }
        }

        // 根据选择的状态过滤预订
        val filteredBookings = when (selectedTab) {
            BookingTab.UPCOMING -> bookingList.filter {
                it.status.equals("Booked", ignoreCase = true) ||
                        it.status.equals("Valid", ignoreCase = true) ||
                        it.status.equals("confirmed", ignoreCase = true)
            }
            BookingTab.PENDING -> bookingList.filter {
                it.status.equals("Pending", ignoreCase = true) ||
                        it.status.equals("waiting", ignoreCase = true)
            }
            BookingTab.CANCELLED -> bookingList.filter {
                it.status.equals("Cancelled", ignoreCase = true) ||
                        it.status.equals("canceled", ignoreCase = true)
            }
            BookingTab.ALL -> bookingList
        }

        // 内容显示
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            filteredBookings.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val emptyMessage = when (selectedTab) {
                        BookingTab.UPCOMING -> "No upcoming bookings"
                        BookingTab.PENDING -> "No pending bookings"
                        BookingTab.CANCELLED -> "No cancelled bookings"
                        BookingTab.ALL -> "No bookings found"
                    }
                    Text(emptyMessage, color = Color.Gray)
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings) { booking ->
                        MyBookingItem(
                            booking = booking,
                            onClick = {
                                navController.navigate("${AppScreen.StudentBookingDetails.name}/${booking.bookingId}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingTabButton(
    tab: BookingTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF2196F3) else Color.Transparent
    val textColor = if (isSelected) Color.White else Color.Gray
    val borderColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { onClick() }
            .background(backgroundColor, MaterialTheme.shapes.small)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            )
    ) {
        Text(
            text = when (tab) {
                BookingTab.UPCOMING -> "Upcoming"
                BookingTab.PENDING -> "Pending"
                BookingTab.CANCELLED -> "Cancelled"
                BookingTab.ALL -> "All"
            },
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun MyBookingItem(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 状态标签和设施类型
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = booking.facility.ifBlank { "Facility" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                StatusBadge(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 场所名称
            Text(
                text = booking.venue.ifBlank { "Unknown Venue" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 日期和时间
            Column {
                if (booking.date.isNotBlank()) {
                    Row {
                        Text(
                            text = "Date: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = booking.date,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
                if (booking.duration.isNotBlank()) {
                    Row {
                        Text(
                            text = "Time: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = booking.duration,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 附加信息
            if (booking.building.isNotBlank() || booking.level.isNotBlank()) {
                Row {
                    if (booking.building.isNotBlank()) {
                        Text(
                            text = booking.building,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (booking.level.isNotBlank()) {
                        if (booking.building.isNotBlank()) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = booking.level,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 预订编号
            if (booking.bookingNo.isNotBlank()) {
                Text(
                    text = "Booking #${booking.bookingNo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (backgroundColor, textColor, displayText) = when (status.lowercase()) {
        "booked", "valid", "confirmed" -> Triple(
            Color(0xFFE8F5E9),
            Color(0xFF388E3C),
            "Valid"
        )
        "pending", "waiting" -> Triple(
            Color(0xFFFFF8E1),
            Color(0xFFF57C00),
            "Pending"
        )
        "cancelled", "canceled" -> Triple(
            Color(0xFFE6AFAF),
            Color(0xFF853838),
            "Cancelled"
        )
        else -> Triple(
            Color(0xFFF5F5F5),
            Color.Gray,
            status.ifBlank { "Booked" }
        )
    }

    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = displayText,
            fontSize = 12.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
