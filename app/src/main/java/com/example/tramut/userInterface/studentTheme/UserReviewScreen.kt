package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.rooms.entity.Review
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.StaffRed
import com.example.tramut.ui.theme.StudentBlue

@Composable
fun ReviewScreen(
    filteredReviews: List<Review>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    containColor: Color
) {

    Column(
        modifier = Modifier
            .background(Background)
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {

        ReviewTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            containColor = containColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = selectedTab,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredReviews) { item ->
                ReviewCard(item,containColor)
            }
        }
    }
}
@Composable
fun ReviewTabs(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    containColor: Color
) {
    val tabs = listOf("All", "Unresolved", "Pending", "Resolved")

    Row(
        modifier = Modifier
            .background((MaterialTheme.colorScheme.background), RoundedCornerShape(20.dp))
            .padding(4.dp)
    ) {
        tabs.forEach { tab ->
            TabItem(
                text = tab,
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                containColor = containColor
            )
        }
    }
}

@Composable
fun TabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    containColor: Color
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(
                if (selected) containColor else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground,
            fontSize = 12.sp
        )
    }
}
@Composable
fun ReviewCard(
    item: Review,
    containColor: Color
) {

    val statusColor = when (item.status) {
        "Unresolved" -> Color.Red
        "Pending" -> Color(0xFFFF9800)
        "Resolved" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF2F2F2)
        ),
        elevation = CardDefaults.cardElevation(5.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.department,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = containColor
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = statusColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            /* ---------- Venue + Type + Date ---------- */
            Text(
                text = item.venue,
                fontSize = 13.sp,
                color = Color.DarkGray
            )

            Text(
                text = "Booking Date: ${item.bookingDate}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            /* ---------- Issue Category（标签） ---------- */
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.background,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = item.issueCategory,
                    fontSize = 11.sp,
                    color = containColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            /* ---------- Comment ---------- */
            Text(
                text = item.comment,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}


@Composable
fun getContainerColor(
    isStudent: Boolean,
    isStaff: Boolean
): Color {
    return when {
        isStudent -> StudentBlue
        isStaff -> StaffRed
        else -> Color.Black
    }
}