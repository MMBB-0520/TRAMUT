package com.example.tramut.userInterface

import android.R.attr.bottom
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfacilitybookingsystem.rooms.entity.Announcement
import com.example.myfacilitybookingsystem.rooms.repo.AnnouncementRepository
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.BorderGray
import com.example.tramut.ui.theme.DateTextGray
import com.example.tramut.ui.theme.TARRed
import com.example.tramut.ui.theme.UMTBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onAnnouncementClick: (String) -> Unit = {},
    bottomBar: @Composable () -> Unit = {}
) {

    val repository = remember { AnnouncementRepository() }
    val announcements = remember { mutableStateListOf<Announcement>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.getAnnouncementsFlow("ALL_PUBLIC").collect { list ->
            announcements.clear()
            announcements.addAll(list.sortedByDescending { it.created_date_str })
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {

        Row {
            // TAR UMT Title
            Text(
                text = "TAR ",
                fontSize = 34.sp,
                color = TARRed,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 8f),  // 阴影偏移
                        blurRadius = 8f           // 模糊半径
                    )
                )
            )
            Text(
                text = "UMT",
                fontSize = 34.sp,
                color = UMTBlue,
                fontWeight = FontWeight.Bold,
                style = TextStyle(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(0f, 8f),
                        blurRadius = 8f
                    )
                )
            )

        }

        Text(
            text = "Facility Booking",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(0f, 8f),  // 阴影偏移
                    blurRadius = 8f           // 模糊半径
                )
            ),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "System",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(0f, 8f),  // 阴影偏移
                    blurRadius = 8f           // 模糊半径
                )
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Date
        val today = remember { LocalDate.now() }
        Text(
            text = today.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE")),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DateTextGray,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Text(
            text = "Announcements",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = UMTBlue)
            }
        } else if (announcements.isEmpty()) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                Text("No announcements currently posted.", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = announcements, key = { it.id }) { item ->
                    AnnouncementCard(
                        announcement = item,
                        onMoreDetails = onAnnouncementClick
                    )
                }
            }
        }


        // Bottom Navigation
        bottomBar()
    }
}


@Composable
fun AnnouncementCard(
    announcement: Announcement,
    onMoreDetails: (String) -> Unit
) {
    Card(
        border = BorderStroke(1.dp, BorderGray),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    text = announcement.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Venue: ${announcement.venue} | Expires: ${announcement.expiry_date_str}",
                fontSize = 12.sp,
                color = DateTextGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onMoreDetails(announcement.id) }
            ) {
                Text(
                    text = "More Details",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = UMTBlue,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = UMTBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        onAnnouncementClick = {},
        bottomBar = {}
    )
}