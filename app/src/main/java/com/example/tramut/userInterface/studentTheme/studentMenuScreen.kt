package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.BlueMain

@Composable
fun StudentMenuScreen(
    name: String,
    studentId: String,
    email: String,
    onMyBookingClick: () -> Unit = {},
    onFacilityBookingClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(52.dp))

        // Profile Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background((BlueMain), RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Profile Picture Placeholder
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .background(Color.LightGray, CircleShape)
                )

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(name,
                        color = Color.White,
                        fontSize = 18.sp)
                    Text(studentId,
                        color = Color.White,
                        fontSize = 14.sp)
                    Text(email,
                        color = Color.White,
                        fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(49.dp))

        // ----- Buttons -----

        MenuButton("My Booking", onClick = onMyBookingClick)
        Spacer(modifier = Modifier.height(40.dp))

        MenuButton("Facility Booking", onClick = onFacilityBookingClick)
        Spacer(modifier = Modifier.height(40.dp))

        MenuButton("Feedback / Review", onClick = onFeedbackClick)
        Spacer(modifier = Modifier.height(40.dp))

        MenuButton("Settings", onClick = onSettingsClick)
        Spacer(modifier = Modifier.height(40.dp))

        // ----- Logout -----

        LogoutButton(onClick = onLogoutClick)
    }
}

@Composable
fun MenuButton(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, fontSize = 14.sp, color = Color.Black)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "Logout", fontSize = 14.sp, color = Color.Red)
        Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Black)
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    StudentMenuScreen(
        name = "John Doe",
        studentId = "123456",
        email = "william.henry.harrison@example-pet-store.com",
        onMyBookingClick = {},
        onFacilityBookingClick = {},
        onFeedbackClick = {},
        onSettingsClick = {},
    )
}
