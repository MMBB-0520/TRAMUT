package com.example.tramut.userInterface.studentTheme

import android.R.attr.onClick
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    containColor: Color,
    onCancelClick: (Review) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
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
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(filteredReviews) { item ->
                ReviewCard(
                    item = item,
                    containColor = containColor,
                    onCancelClick = { onCancelClick(item)})
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
    val tabs = listOf("All", "Unsolved", "Pending", "Solved")

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
    containColor: Color,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .then(
                if (item.status != "Solved") {
                    Modifier.clickable { onCancelClick() }
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Status(status = item.status)

                Text(
                    text = item.bookingDate,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Venue Title
            Text(
                text = item.venue,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )

            // Category (Uses the theme's containColor for consistency)
            Text(
                text = item.issueCategory,
                color = containColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = Color.LightGray
            )

            // Bottom section: Department and Comment
            Text(
                text = "To ${item.department} Department: ",
                fontSize = 11.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.comment,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun Status(status: String) {
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