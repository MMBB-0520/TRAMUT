package com.example.tramut.userInterface.studentTheme

import android.R.attr.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSportScreen(
    selectedVenueFromPrevious: String,
    selectedDateFromPrevious: String = "",
    onSubmit: (String, String, String, String) -> Unit
) {
    var termsAccepted by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Use the passed date if available, otherwise default
    var selectedDate by remember {
        mutableStateOf(
            if (selectedDateFromPrevious.isNotEmpty())
                selectedDateFromPrevious
            else
                "15 / Nov / 2025 (Sat)"
        )
    }
    val dateList = listOf(
        "14 / Nov / 2025 (Fri)",
        "15 / Nov / 2025 (Sat)",
        "16 / Nov / 2025 (Sun)"
    )

    var selectedStartTime by remember { mutableStateOf("09:00 AM") }
    val timeList = listOf(
        "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM",
        "6:00 PM", "7:00 PM", "8:00 PM", "9:00 PM"
    )

    var selectedEndTime by remember { mutableStateOf("11:00 AM") }
    val endTimeList = listOf(
        "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM",
        "5:00 PM", "6:00 PM", "7:00 PM", "8:00 PM",
        "9:00 PM", "10:00 PM"
    )

    // Use the passed venue if available, otherwise default
    var selectedVenue by remember {
        mutableStateOf(
            if (selectedVenueFromPrevious.isNotEmpty())
                selectedVenueFromPrevious
            else
                "Club House - Squash"
        )
    }
    val venueList = listOf(
        "Club House - Squash",
        "Club House-Swimming Pool",
        "Club House-Guest/Karaoke Room",
        "Club House-Gym 1 (11am-1pm, 3pm-5pm & 7pm-9pm)",
        "Club House-Gym 1 (9am-11am, 1pm-3pm & 5pm-7pm)",
        "Club House-S.P.Foyer (11am-1pm & 3pm-5pm)",
        "Club House-S.P.Foyer (9am-11am & 1pm-3pm)",
        "Club House-Snooker",
        "Sports Complex-Badminton",
        "Sports Complex-Gym 3 (11am-1pm, 3pm-5pm & 7pm-9pm)",
        "Sports Complex-Gym 3 (9am-11am, 1pm-3pm & 5pm-7pm)",
        "Sports Complex-Pickleball",
        "Sports Complex-Table Tennis",
        "Sports Complex-Tennis",
        "Sports Complex-Futsal",
        "TAR UMT Arena-Futsal"
    )

    Box(
        modifier = Modifier
            .padding(18.dp)
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TextField(
                value = "Sports Facilities",
                onValueChange = {},
                readOnly = true,
                label = { Text("Facility") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,

                    focusedIndicatorColor = Color.Black,
                    unfocusedIndicatorColor = Color.Gray,
                    disabledIndicatorColor = Color.LightGray
                )
            )

            Spacer(Modifier.height(20.dp))

            DropdownUnderlinedTextField("Booking Date", selectedDate, dateList) { selectedDate = it }

            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    DropdownUnderlinedTextField("Start Time", selectedStartTime, timeList) { selectedStartTime = it }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    DropdownUnderlinedTextField("End Time", selectedEndTime, endTimeList) { selectedEndTime = it }
                }
            }
            Spacer(Modifier.height(20.dp))

            DropdownUnderlinedTextField("Venue Type", selectedVenue, venueList) { selectedVenue = it }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFEF), shape = RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Terms of Use:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    val terms = listOf(
                        "The booked facilities will be forfeited after 15 minutes if no-show, except for futsal court.",
                        "Check-in/Check-out at the Counter. All users/participants must present their ID cards at the counter upon using sports facilities.",
                        "The booked facilities can only be used for the designated function of the facilities, unless with prior approval of the management. If players wish to use the facility for any other activity, the players should seek advice and approval from Department of Student Affairs(DSA) before making the booking.",
                        "Players should be properly attired at all times. Shorts and T-shirts must be worn at all times and sports shoes are mandatory. For squash and badminton, only non-marking sports shoes within the court.",
                        "For gym equipment use, towels are compulsory. All users are required to bring their own towel (not handkerchief/shirt) to wipe their own sweat and other hygienic purposes. Gym users are not allowed to share towels.",
                        "Foods and bags are not allowed at the sports venue. Drinks and water may be taken in non-breakable, spill-proof containers. Smoking and Vaping is strictly prohibited in the campus.",
                        "For badminton, tennis and pickleball, maximum 8 players per court at one time.",
                        "For table tennis, squash and snooker, maximum 6 players per court at one time.",
                        "Students are not permitted to bring guests/outsiders under their booking.",
                        "Players must leave the facility/playing area when their booked session/hour is over. All hired/borrowed equipment should be returned at the same time.",
                        "The University reserves the right to add, change, withdraw or cancel any booking without prior notice. This includes closing a facilities or changes to its opening hours for safety reasons, maintenance or special events.",
                        "The University reserves the right to bar anyone who does not observe the Rules and Regulations for Sports Facilities, abuses equipment or shows disrespect to other students and staff or are in violation of the Student Code of Conduct."
                    )

                    terms.forEachIndexed { index, term ->
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append("${index + 1}. ")
                                }
                                append(term)
                            },
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(bottom = 8.dp),
                            style = LocalTextStyle.current.copy(
                                textIndent = TextIndent(firstLine = 0.sp, restLine = 20.sp)
                            )
                        )
                    }

                    Text(
                        text = "# Rules & Regulations are subject to change by TAR UMT from time to time. Users may be notified of such changes in any manner deemed appropriate by TAR UMT.",
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "I have read and agreed to terms of use.",
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it }
                )
            }

            Button(
                onClick = {
                    showSuccessDialog = true
                },
                enabled = termsAccepted,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SUBMIT")
            }

            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showSuccessDialog = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                // Pass all 4 parameters correctly
                                onSubmit(selectedVenue, selectedDate, selectedStartTime, selectedEndTime)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(2.dp)
                        ) {
                            Text("OK", color = Color.White, fontSize = 24.sp)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .background(Color(0xFF4CAF50), shape = RoundedCornerShape(50)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = com.example.tramut.R.drawable.correct),
                                    contentDescription = "SuccessBook",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(50.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Booking successfully submitted!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    },
                    modifier = Modifier.background(Color.White, shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownUnderlinedTextField(
    label: String,
    value: String,
    items: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {

        TextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,

                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Gray,
                disabledIndicatorColor = Color.LightGray
            ),
            singleLine = true
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onValueChange(item)
                        expanded = false
                    }
                )
            }
        }
    }
}