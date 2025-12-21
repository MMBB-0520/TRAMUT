package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
import com.example.tramut.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacilityBookScreen(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavHostController,
    userId: String,
    onCITCABClick: () -> Unit,
    onLibraryABClick: () -> Unit,
    onSportsABClick: () -> Unit,
    onCITCTTClick: () -> Unit,
    onLibraryTTClick: () -> Unit,
    onSportsTTClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {

        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { onTabSelected(0) },
                text = { Text("Facility Booking") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { onTabSelected(1) },
                text = { Text("My Bookings") }
            )
        }
        when (selectedTabIndex) {
            0 -> {
                FacilityItemUI("Cyber Centre Discussion Room", onCITCABClick, onCITCTTClick)
                FacilityItemUI("Library Discussion Room", onLibraryABClick, onLibraryTTClick)
                FacilityItemUI("Sports Facilities", onSportsABClick, onSportsTTClick)
            }
            1 -> {
                MyBookingScreen(userId = userId, navController = navController)
            }
        }
    }
}

@Composable
fun FacilityItemUI(name: String, onAddBookingClick: () -> Unit, onViewTTClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)

    ) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(name, modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.add),
                contentDescription = "Add",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onAddBookingClick() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                painter = painterResource(id = R.drawable.time),
                contentDescription = "Time",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onViewTTClick() }
            )
        }
    }
}