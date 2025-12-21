package com.example.tramut.userInterface.studentTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tramut.rooms.entity.Member
import com.example.tramut.rooms.repo.MembersValidationResult
import com.example.tramut.rooms.repo.UsersRepo
import com.example.tramut.ui.theme.StaffRed
import com.example.tramut.ui.theme.StudentBlue
import com.example.tramut.viewModel.VenueViewModel
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch
import com.example.tramut.userInterface.studentTheme.FacilityData
import com.example.tramut.viewModel.MyBookingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookSportScreen(
    facilityType: String,
    selectedDateFromPrevious: String = "",
    onBackFacilityPage: () -> Unit,
    onSubmit: (String, String, String, String, Int, List<Member>, String, String) -> Unit,
    isStaff: Boolean = false,
    userRepository: UsersRepo
) {
    val containerColor = if (isStaff) StaffRed else StudentBlue
    var termsAccepted by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showTimeErrorDialog by remember { mutableStateOf(false) }
    var timeErrorMessage by remember { mutableStateOf("") }

    // 新增验证相关状态
    var showValidationError by remember { mutableStateOf(false) }
    var validationErrorMessage by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var validationSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var numberOfPax by remember { mutableStateOf("1") }

    // check num member
    val members = remember(numberOfPax) {
        val pax = numberOfPax.toIntOrNull() ?: 1
        // pax 1-15
        val validPax = pax.coerceIn(1, 15)
        if (validPax != pax) {
            numberOfPax = validPax.toString()
        }
        MutableList(validPax) { Member(id = "", name = "") }
    }

    // 使用 ViewModel 获取场地列表
    val venueViewModel: VenueViewModel = viewModel()
    val venueState by venueViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // facilityType -> department 映射
    val department = when (facilityType) {
        "Library" -> "Library"
        "Cyber Centre", "CITC" -> "CITC"
        else -> "Sport Facilities"
    }

    val venueList = remember(department) {
        FacilityData.getCategoriesForDepartment(department)
    }

    // 当进入页面时加载场地
    LaunchedEffect(facilityType) {
        venueViewModel.loadVenues(facilityType)
    }

    // 根据 facilityType 确定场地来源
    val (facilityName, terms, showMemberDetails, notes) = when (facilityType) {
        "Cyber Centre" -> Quadruple(
            "Cyber Centre Discussion Room",
            listOf(
                "Read the Booking Guidelines.",
                "Discussion rooms are for study purposes only.",
                "Do not hog the discussion room if it is not in use.",
                "Do not lock the discussion room door.",
                "Foods and beverages are prohibited in the discussion room.",
                "Student must CHECK IN / CHECK OUT at Internet Lab Counter.",
                "Room booked will be forfeited after 10 minutes if no-show.",
                "Students shall discuss softly so as not to disturb other users.",
                "Remember to take all your belongings with you when leave the room.",
                "Always keep the room clean.",
                "Do not add in / move / remove the furniture inside the discussion room.",
                "No vandalism is allowed inside the discussion room."
            ),
            true,
            listOf(
                "Discussion rooms are strictly to be used for academic purpose only.",
                "Projecting movie from projector is prohibited due to copyright issue."
            )
        )
        "Library" -> Quadruple(
            "Library Discussion Room / Individual Study Room",
            listOf(
                "Must read the 'Booking Guidelines' before proceeding for booking.",
                "Artwork, role-play, video shooting and/or any other disruptive activities are not allowed in the Library.",
                "Discussion Rooms are solely intended for written assignments only.",
                "Presentation Room is strictly to be used for presentation purposes only.",
                "Users using the Discussion Room and Presentation Room shall discuss softly.",
                "Library staff reserve the right to conduct occasional spot checks of the rooms.",
                "Return the room key and complete the check-out process on time.",
                "Users shall clear all belongings and books from the room while leaving.",
                "Arrange tables and chairs to their original position.",
                "Shut down the PC or LCD projector, if applicable.",
                "Switch off the lights.",
                "Lock the room upon leaving."
            ),
            true,
            listOf(
                "Library Rules and Regulations applied. Users may be asked to leave immediately if do not comply with the rules."
            )
        )
        else -> Quadruple(
            "Sports Facilities",
            listOf(
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
            ),
            false,
            emptyList()
        )
    }

    // book for 3 days
    val dateList = remember {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy / MMM / dd (EEE)", Locale.ENGLISH)

        List(3) { i ->
            calendar.time = Date() // set to today
            calendar.add(Calendar.DAY_OF_YEAR, i)
            formatter.format(calendar.time)
        }
    }

    var selectedDate by remember { mutableStateOf("") }

    // 如果从上一页传入了日期，设置它
    LaunchedEffect(selectedDateFromPrevious) {
        if (selectedDateFromPrevious.isNotEmpty() && selectedDate.isEmpty()) {
            selectedDate = selectedDateFromPrevious
        }
    }

    // check start & end time
    fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val cleanedTime = timeStr.replace(".", "").trim()
            val isPM = cleanedTime.contains("PM", ignoreCase = true)
            val isAM = cleanedTime.contains("AM", ignoreCase = true)
            val timeWithoutAmPm = cleanedTime.replace("AM", "")
                .replace("PM", "")
                .replace("am", "")
                .replace("pm", "")
                .trim()

            val parts = timeWithoutAmPm.split(":")
            val hour = parts[0].toInt()
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            var totalMinutes = when {
                isPM && hour == 12 -> 12 * 60
                isPM -> (hour + 12) * 60
                isAM && hour == 12 -> 0
                else -> hour * 60
            }
            totalMinutes += minute

            totalMinutes
        } catch (e: Exception) {
            0
        }
    }

    fun validateTimes(startTime: String, endTime: String): Pair<Boolean, String> {
        val startMinutes = parseTimeToMinutes(startTime)
        val endMinutes = parseTimeToMinutes(endTime)

        if (startMinutes >= endMinutes) {
            return Pair(false, "Start time must be earlier than end time.")
        }

        val duration = endMinutes - startMinutes
        if (duration > 120) {
            return Pair(false, "Maximum booking duration is 2 hours.")
        }

        return Pair(true, "")
    }

    var selectedStartTime by remember { mutableStateOf("") }
    val timeList = listOf(
        "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM",
        "6:00 PM", "7:00 PM", "8:00 PM", "9:00 PM"
    )

    var selectedEndTime by remember { mutableStateOf("") }
    val endTimeList = listOf(
        "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM",
        "5:00 PM", "6:00 PM", "7:00 PM", "8:00 PM",
        "9:00 PM", "10:00 PM"
    )

    fun handleStartTimeChange(newStartTime: String) {
        selectedStartTime = newStartTime

        val (isValid, errorMessage) = validateTimes(newStartTime, selectedEndTime)
        if (!isValid) {
            val startMinutes = parseTimeToMinutes(newStartTime)
            val validEndTimes = endTimeList.filter { endTime ->
                val endMinutes = parseTimeToMinutes(endTime)
                endMinutes > startMinutes && (endMinutes - startMinutes) <= 120
            }

            selectedEndTime = validEndTimes.firstOrNull() ?: selectedEndTime
        }
    }

    fun handleEndTimeChange(newEndTime: String) {
        val (isValid, errorMessage) = validateTimes(selectedStartTime, newEndTime)
        if (isValid) {
            selectedEndTime = newEndTime
        } else {
            timeErrorMessage = errorMessage
            showTimeErrorDialog = true
        }
    }

    var selectedVenue by remember { mutableStateOf("") }

    // 验证成员函数
    fun validateMembers(): Boolean {
        if (!showMemberDetails) return true

        val totalMembers = numberOfPax.toIntOrNull() ?: 1
        val membersToValidate = if (totalMembers > 1) {
            members.subList(1, minOf(totalMembers, members.size))
                .filter { it.id.isNotBlank() }
        } else {
            emptyList()
        }

        if (membersToValidate.isEmpty()) return true


        // 检查重复ID
        val loginIds = membersToValidate.map { it.id }
        val duplicateIds = loginIds.groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }
            .keys

        if (duplicateIds.isNotEmpty()) {
            validationErrorMessage = "Duplicate student IDs found: ${duplicateIds.joinToString(", ")}"
            showValidationError = true
            return false
        }

        return true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackFacilityPage) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("New Booking", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Facility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        Text(
                            text = "Facility",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = facilityName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        Divider(
                            color = Color.Gray,
                            thickness = 1.dp
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Booking Date
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    DropdownUnderlinedTextFieldSimple(
                        value = selectedDate,
                        items = dateList,
                        label = "Booking Date *",
                        onValueChange = { selectedDate = it }
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Start Time and End Time
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column {
                        Row(Modifier.fillMaxWidth()) {
                            Column(Modifier.weight(1f)) {
                                DropdownUnderlinedTextFieldSimple(
                                    value = selectedStartTime,
                                    items = timeList,
                                    label = "Start Time *",
                                    onValueChange = { handleStartTimeChange(it) }
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                DropdownUnderlinedTextFieldSimple(
                                    value = selectedEndTime,
                                    items = endTimeList,
                                    label = "End Time *",
                                    onValueChange = { handleEndTimeChange(it) }
                                )
                            }
                        }
                        // 显示时长信息
                        val startMinutes = parseTimeToMinutes(selectedStartTime)
                        val endMinutes = parseTimeToMinutes(selectedEndTime)
                        val duration = endMinutes - startMinutes

                        if (duration > 0) {
                            val hours = duration / 60
                            val minutes = duration % 60
                            val durationText = when {
                                hours > 0 && minutes > 0 -> "$hours hour${if (hours > 1) "s" else ""} $minutes minute${if (minutes > 1) "s" else ""}"
                                hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
                                else -> "$minutes minute${if (minutes > 1) "s" else ""}"
                            }

                            Text(
                                text = "Duration: $durationText (Max: 2 hours)",
                                fontSize = 12.sp,
                                color = if (duration <= 120) Color.Gray else Color.Red,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                // Venue Type - 添加加载状态显示
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    if (venueState.isLoading) {
                        Column {
                            Text(
                                text = "Venue Type *",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Loading venues...",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            Divider(
                                color = Color.Gray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else if (venueState.error != null && venueList.isEmpty()) {
                        Column {
                            Text(
                                text = "Venue Type *",
                                fontSize = 12.sp,
                                color = Color.Red,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Failed to load venues",
                                    fontSize = 14.sp,
                                    color = Color.Red,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                text = "Using default list",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Divider(
                                color = Color.Gray,
                                thickness = 1.dp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    } else if (venueList.isEmpty()) {
                        Column {
                            Text(
                                text = "Venue Type *",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                            Text(
                                text = "No venues available",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Divider(
                                color = Color.Gray,
                                thickness = 1.dp
                            )
                        }
                    } else {
                        DropdownUnderlinedTextFieldSimple(
                            value = selectedVenue,
                            items = venueList,
                            label = "Venue Type *",
                            onValueChange = { selectedVenue = it }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Number of Pax (Cyber Centre和Library显示)
                if (showMemberDetails) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column {
                            LineTextField(
                                value = numberOfPax,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() } && newValue.isNotEmpty()) {
                                        val num = newValue.toIntOrNull() ?: 1
                                        if (num in 1..15) {
                                            numberOfPax = newValue
                                        } else if (num > 15) {
                                            numberOfPax = "15"
                                        }
                                    } else if (newValue.isEmpty()) {
                                        numberOfPax = ""
                                    }
                                },
                                label = "Number of Pax *",
                                placeholder = "Enter number of people (1-15)",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Text(
                                text = "Max 15 persons (including yourself)",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))

                    // Member's Details (Cyber Centre和Library显示)
                    val totalMembers = numberOfPax.toIntOrNull() ?: 1
                    val membersToShow = if (totalMembers > 1) totalMembers - 1 else 0

                    if (membersToShow > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Member's Details",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Student Id",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "Student Name",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Divider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color.Gray,
                                    thickness = 1.dp
                                )

                                for (index in 0 until membersToShow) {
                                    val memberIndex = index + 1
                                    val member = members[memberIndex]

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = "Member ${index + 1}",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            LineTextField(
                                                value = member.id,
                                                onValueChange = { newId ->
                                                    if (memberIndex < members.size) {
                                                        // 更新对象属性
                                                        members[memberIndex] = members[memberIndex].copy(id = newId)
                                                    }
                                                },
                                                label = "ID"
                                            )
                                        }

                                        Spacer(Modifier.width(16.dp))

                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = "Name",
                                                fontSize = 12.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                            LineTextField(
                                                value = member.name,
                                                onValueChange = { newName ->
                                                    if (memberIndex < members.size) {
                                                        members[memberIndex] = members[memberIndex].copy(name = newName)
                                                    }
                                                },
                                                label = "Name"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                    }
                }

                // Terms of Use
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

                        // Add notes for Cyber Centre
                        if (notes.isNotEmpty() && facilityType == "Cyber Centre") {
                            Spacer(Modifier.height(12.dp))
                            notes.forEachIndexed { index, note ->
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                            append("NOTE ${index + 1}: ")
                                        }
                                        append(note)
                                    },
                                    fontSize = 16.sp,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    style = LocalTextStyle.current.copy(
                                        textIndent = TextIndent(firstLine = 0.sp, restLine = 20.sp)
                                    )
                                )
                            }
                        }

                        // Add important notice for Library
                        if (facilityType == "Library") {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Important: " + notes.firstOrNull() ?: "",
                                fontSize = 16.sp,
                                lineHeight = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        Text(
                            text = "# Rules & Regulations are subject to change by TAR UMT from time to time. Users may be notified of such changes in any manner deemed appropriate by TAR UMT.",
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(120.dp))
            }

            // 固定在底部的复选框和提交按钮
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
                        onCheckedChange = {
                            termsAccepted = it
                            validationSuccess = false // 重置验证状态
                        }
                    )
                }

                Button(
                    onClick = {
                        if (!termsAccepted) return@Button

                        // 先验证时间
                        val (isValid, errorMessage) = validateTimes(selectedStartTime, selectedEndTime)
                        if (!isValid) {
                            timeErrorMessage = errorMessage
                            showTimeErrorDialog = true
                            return@Button
                        }

                        // 验证基本表单字段
                        if (selectedDate.isEmpty() || selectedStartTime.isEmpty() ||
                            selectedEndTime.isEmpty() || selectedVenue.isEmpty()) {
                            validationErrorMessage = "Please fill in all required fields (*)"
                            showValidationError = true
                            return@Button
                        }

                        if (showMemberDetails && !validateMembers()) {
                            return@Button
                        }

                        if (validationSuccess) {
                            showSuccessDialog = true
                            return@Button
                        }

                        val totalMembers = numberOfPax.toIntOrNull() ?: 1
                        val membersToValidate = if (totalMembers > 1 && showMemberDetails) {
                            members.subList(1, minOf(totalMembers, members.size))
                                .filter { it.id.isNotBlank() }
                        } else {
                            emptyList()
                        }

                        if (membersToValidate.isNotEmpty()) {
                            isVerifying = true
                            coroutineScope.launch {
                                val result = userRepository.validateMembersWithDuplicates(membersToValidate)
                                isVerifying = false

                                when (result) {
                                    is MembersValidationResult.Success -> {
                                        validationSuccess = true
                                        showSuccessDialog = true
                                    }
                                    is MembersValidationResult.DuplicatesFound -> {
                                        validationErrorMessage = "Duplicate student IDs found: ${result.duplicates.joinToString(", ")}"
                                        showValidationError = true
                                    }
                                    is MembersValidationResult.InvalidIds -> {
                                        validationErrorMessage = "Invalid student IDs: ${result.invalidIds.joinToString(", ")}"
                                        showValidationError = true
                                    }
                                    is MembersValidationResult.Error -> {
                                        validationErrorMessage = result.errorMessage
                                        showValidationError = true
                                    }

                                    else -> {}
                                }
                            }
                        } else {
                            validationSuccess = true
                            showSuccessDialog = true
                        }
                    },
                    enabled = termsAccepted && !isVerifying,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = containerColor
                    )
                ) {
                    if (isVerifying) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("VERIFYING...", color = Color.White)
                        }
                    } else {
                        Text("SUBMIT", color = Color.White)
                    }
                }
            }

            if (showTimeErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showTimeErrorDialog = false },
                    title = {
                        Text("Invalid Time Selection", color = Color.Red)
                    },
                    text = {
                        Text(timeErrorMessage)
                    },
                    confirmButton = {
                        Button(
                            onClick = { showTimeErrorDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = containerColor)
                        ) {
                            Text("OK")
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (showValidationError) {
                AlertDialog(
                    onDismissRequest = { showValidationError = false },
                    title = {
                        Text("Validation Error", color = Color.Red)
                    },
                    text = {
                        Text(validationErrorMessage)
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showValidationError = false
                                validationSuccess = false // 重置验证状态
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = containerColor)
                        ) {
                            Text("OK")
                        }
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }

            if (showSuccessDialog) {
                val bookingViewModel: MyBookingViewModel = viewModel() // 获取计算工具

                SuccessDialog(
                    onOk = {
                        val level = bookingViewModel.getLevelForVenue(selectedVenue)
                        val building = bookingViewModel.getBuildingForVenue(selectedVenue)
                        showSuccessDialog = false
                        val pax = numberOfPax.toIntOrNull() ?: 1

                        val currentMembers = if (pax > 1 && members.size > 1) {
                            members.drop(1).take(pax - 1)
                        } else {
                            emptyList()
                        }
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        val currentUserId = auth.currentUser?.uid ?: ""

                        bookingViewModel.manualAutoAssignAndSave(
                            category = selectedVenue,
                            date = selectedDate,
                            startTime = selectedStartTime,
                            endTime = selectedEndTime,
                            userId = currentUserId,
                            pax = pax,
                            members = currentMembers,
                            level = level,
                            building = building,
                            onResult = { success, message ->
                                if (success) {
                                    // 4. Handle Success
                                    showSuccessDialog = false
                                    coroutineScope.launch {
                                        delay(500)
                                        onBackFacilityPage()
                                    }
                                } else {
                                    // 5. Handle Failure (e.g., No courts available)
                                    // You might want to show a different error dialog or a Toast here
                                    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                                    showSuccessDialog = false
                                }
                            }
                        )

                        onSubmit(selectedVenue, selectedDate, selectedStartTime, selectedEndTime, pax, currentMembers, level, building)

                        coroutineScope.launch  {
                            delay(500)
                            onBackFacilityPage()
                        }

                        // 重置状态
                        validationSuccess = false
                        termsAccepted = false
                        isVerifying = false
                    },
                    onDismiss = {
                        showSuccessDialog = false
                        validationSuccess = false
                    }
                )
            }
        }
    }
}

data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@Composable
fun DropdownUnderlinedTextFieldSimple(
    value: String,
    items: List<String>,
    label: String,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hasValue = value.isNotEmpty()

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        ) {
            Column {
                if (hasValue) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (hasValue) value else label,
                        fontSize = if (hasValue) 16.sp else 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (hasValue) Color.Black else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Color.Gray
                    )
                }
            }
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
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

@Composable
fun LineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true
) {
    val hasValue = value.isNotEmpty()
    var text by remember { mutableStateOf(value) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(value) {
        text = value
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusRequester.requestFocus()
                }
        ) {
            Column {
                if (hasValue) {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onValueChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = if (hasValue) 0.dp else 8.dp)
                        .focusRequester(focusRequester),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = if (hasValue) 16.sp else 14.sp,
                        color = if (hasValue) Color.Black else Color.Gray
                    ),
                    keyboardOptions = keyboardOptions,
                    singleLine = singleLine,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = placeholder.ifEmpty { label },
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Divider(
            color = Color.Gray,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}