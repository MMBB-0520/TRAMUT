package com.example.myfacilitybookingsystem.userInterface.adminTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import com.example.myfacilitybookingsystem.AppScreen
import com.example.myfacilitybookingsystem.viewModel.AdminsViewModel
import com.example.myfacilitybookingsystem.ui.theme.Background
import com.example.myfacilitybookingsystem.ui.theme.BorderGray
import com.example.myfacilitybookingsystem.ui.theme.LogoutRed

@Composable
fun AdminMainScreen(
    navController: NavController,
    viewModel: AdminsViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val adminData = viewModel.adminUser.value
    val isLoading = viewModel.isLoading.value

    Scaffold(containerColor = Background) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp).verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Admin Info
            if (isLoading || adminData == null) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.Black, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color.White) }
            } else {
                AdminInfoCard(name = adminData.name.ifEmpty { "Admin" }, dept = adminData.department.ifEmpty { "General" }, email = adminData.email.ifEmpty { "" })
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Check In/Out
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExpandedDashboardButton("Check In", Icons.Default.CheckCircle, {}, Modifier.weight(1f))
                ExpandedDashboardButton("Check Out", Icons.AutoMirrored.Filled.ExitToApp, {}, Modifier.weight(1f))
            }

            // 3. Menus
            ExpandableDashboardMenu("Announcement", Icons.Default.Notifications, listOf("View Announcements" to { navController.navigate(AppScreen.ViewAn.name) }, "Post Announcement" to { navController.navigate(AppScreen.PostAn.name) }))
            ExpandableDashboardMenu("Facility", Icons.Default.Build, listOf("Edit / Delete Facility" to { navController.navigate(AppScreen.EditFac.name) }, "Add New Facility" to { navController.navigate(AppScreen.AddFac.name) }))
            DashboardButton("Reviews", Icons.Default.Star, { navController.navigate(AppScreen.AdminViewReview.name) })

            Spacer(modifier = Modifier.weight(1f))

            // 4. Logout
            LogoutButton {
                viewModel.performLogout {
                    navController.navigate(AppScreen.AdminLoginScreen.name) { popUpTo(0) }
                }
            }
        }
    }
}

@Composable
fun AdminInfoCard(name: String, dept: String, email: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Black), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Color.Black, modifier = Modifier.size(36.dp)) }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(dept, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = Color.LightGray)
                Text(email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun DashboardButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BorderGray), color = Color.White, modifier = Modifier.fillMaxWidth().height(60.dp)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun ExpandedDashboardButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, BorderGray), color = Color.White, modifier = modifier.height(60.dp)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ExpandableDashboardMenu(title: String, icon: ImageVector, options: List<Pair<String, () -> Unit>>) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(if (expanded) 180f else 0f, label = "rotation")
    Column(modifier = Modifier.fillMaxWidth().border(BorderStroke(1.dp, BorderGray), RoundedCornerShape(12.dp)).background(Color.White, RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))) {
        Row(modifier = Modifier.fillMaxWidth().height(60.dp).clickable { expanded = !expanded }.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, "Drop Down", modifier = Modifier.rotate(rotationState), tint = Color.Gray)
        }
        AnimatedVisibility(visible = expanded) {
            Column {
                HorizontalDivider(color = Color(0xFFF0F0F0))
                options.forEach { (text, onClick) ->
                    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp, horizontal = 56.dp)) { Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray) }
                    HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(start = 56.dp))
                }
            }
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color.White), border = BorderStroke(1.dp, BorderGray), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(56.dp)) {
        Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = LogoutRed)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Logout", color = LogoutRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}