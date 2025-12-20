package com.example.myfacilitybookingsystem.userInterface.adminTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.tramut.AppScreen
import com.example.myfacilitybookingsystem.viewModel.AdminsViewModel
import com.example.tramut.ui.theme.Background
// If BlueMain is not in your imports, uncomment the line below:
// val BlueMain = Color(0xFF0066FF)
import com.example.tramut.ui.theme.BlueMain
import com.example.tramut.ui.theme.StaffRed

@Composable
fun AdminMainScreen(
    navController: NavController,
    onClick: () -> Unit,
    viewModel: AdminsViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val adminData = viewModel.adminUser.value
    val isLoading = viewModel.isLoading.value

    Scaffold(containerColor = Background) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp) // Updated padding to match Student Theme
                .verticalScroll(scrollState)
        ) {

            Spacer(modifier = Modifier.height(52.dp))

            // 1. Admin Info Card (Blue Style)
            if (isLoading || adminData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueMain)
                }
            } else {
                AdminProfileCard(
                    name = adminData.name.ifEmpty { "Admin" },
                    dept = adminData.department.ifEmpty { "General" },
                    email = adminData.email.ifEmpty { "" }
                )
            }

            Spacer(modifier = Modifier.height(49.dp))

            // 2. Dashboard Buttons (Styled like MenuButton)

            // Check In
            AdminMenuButton(
                text = "Check In",
                icon = Icons.Default.CheckCircle,
                onClick = { navController.navigate(AppScreen.CheckInScanner.name) }
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Check Out
            AdminMenuButton(
                text = "Check Out",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                onClick = { navController.navigate(AppScreen.CheckOutScanner.name) }
            )
            Spacer(modifier = Modifier.height(40.dp))

            // 3. Expandable Menus (Styled to match, logic preserved)

            ExpandableAdminMenu(
                title = "Announcement",
                icon = Icons.Default.Notifications,
                options = listOf(
                    "View Announcements" to { navController.navigate(AppScreen.ViewAn.name) },
                    "Post Announcement" to { navController.navigate(AppScreen.PostAn.name) }
                )
            )
            Spacer(modifier = Modifier.height(40.dp))

            ExpandableAdminMenu(
                title = "Facility",
                icon = Icons.Default.Build,
                options = listOf(
                    // 1. Existing: Edit
                    "Edit / Delete Facility" to { navController.navigate(AppScreen.EditFac.name) },

                    // 2. Existing: Add
                    "Add New Facility" to { navController.navigate(AppScreen.AddFac.name) },

                    // 3. NEW: Preview Timetable
                    // We pass the admin's department to the route so the timetable filters correctly
                    "Preview Timetable" to {
                        val dept = adminData?.department ?: "General"
                        navController.navigate("${AppScreen.ViewTimetable.name}/$dept")
                    }
                )
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Reviews
            AdminMenuButton(
                text = "Reviews",
                icon = Icons.Default.Star,
                onClick = { navController.navigate(AppScreen.ViewTimetable.name) }
            )
            Spacer(modifier = Modifier.height(40.dp))

            // 4. Logout (Red Style)
            AdminLogoutButton {
                viewModel.performLogout {
                    navController.navigate(AppScreen.AdminLoginScreen.name) { popUpTo(0) }
                }
            }

            Spacer(modifier = Modifier.height(20.dp)) // Extra bottom padding
        }
    }
}

// --- COMPONENTS ---

@Composable
fun AdminProfileCard(name: String, dept: String, email: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color= Color.Black, RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .background(Color.LightGray, CircleShape)
            ){
                Text(
                    text = (name).take(1).uppercase(),
                    color = StaffRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 30.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column {
                Text(name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(dept, color = Color.White, fontSize = 14.sp)
                Text(email, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun AdminMenuButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
        Spacer(modifier = Modifier.width(16.dp))

        Text(text = text, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Black)
    }
}

@Composable
fun ExpandableAdminMenu(title: String, icon: ImageVector, options: List<Pair<String, () -> Unit>>) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expanded) 180f else 0f, label = "rotation")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .background(Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))

            Text(title, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.weight(1f))

            Icon(Icons.Default.ArrowDropDown, "Drop Down", modifier = Modifier.rotate(rotationState), tint = Color.Black)
        }

        // Expanded Content (Logic preserved)
        AnimatedVisibility(visible = expanded) {
            Column {
                HorizontalDivider(color = Color(0xFFE0E0E0))
                options.forEach { (text, onClick) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onClick() }
                            .padding(vertical = 12.dp, horizontal = 56.dp) // Indented to align with text above
                    ) {
                        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}

@Composable
fun AdminLogoutButton(onClick: () -> Unit) {
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
        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Black)
    }
}