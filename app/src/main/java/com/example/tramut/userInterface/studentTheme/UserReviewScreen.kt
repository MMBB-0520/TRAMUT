package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.rooms.entity.Review


@Composable
fun ReviewScreen(
    reviews: List<Review>
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
    ) {

        // Tabs
        ReviewTabs()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "All",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(reviews) { item ->
                ReviewCard(item)
            }
        }
    }
}
@Composable
fun ReviewTabs() {
    Row(
        modifier = Modifier
            .background(Color(0xFFEDEDED), RoundedCornerShape(20.dp))
            .padding(4.dp)
    ) {
        TabItem("All", true)
        TabItem("Unresolved", false)
        TabItem("Pending", false)
        TabItem("Resolved", false)
    }
}

@Composable
fun TabItem(text: String, selected: Boolean) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .background(
                if (selected) Color(0xFF1E2BD8) else Color.Transparent,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontSize = 12.sp
        )
    }
}
@Composable
fun ReviewCard(item: Review) {

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
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.venue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFF1E2BD8)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.comment,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.status,
                fontSize = 12.sp,
                color = statusColor
            )
        }
    }
}
