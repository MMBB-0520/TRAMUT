package com.example.tramut

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.checkincompose.BarcodeScannerScreen
import com.example.tramut.userInterface.HomeScreen
import com.example.tramut.userInterface.loginTheme.StaffLoginScreen
import com.example.tramut.userInterface.staffTheme.StaffMenuScreen
import com.example.tramut.userInterface.studentTheme.StudentMenuScreen
import com.example.tramut.ui.theme.StaffRed
import com.example.tramut.ui.theme.StudentBlue
import com.example.myfacilitybookingsystem.userInterface.adminTheme.AdminMainScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.AdminAnnouncementScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.AnnouncementDetailScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.EditAnnouncementScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.PostAnnouncementScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility.AdminAddFacilityScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility.EditFacilityScreen
import com.example.myfacilitybookingsystem.userInterface.loginTheme.AdminLoginScreen
import com.example.myfacilitybookingsystem.viewModel.AdminsViewModel
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.repo.UsersRepo
import com.example.tramut.ui.theme.StaffRed
import com.example.tramut.ui.theme.StudentBlue
import com.example.tramut.userInterface.HomeScreen
import com.example.tramut.userInterface.TimetableScreen
import com.example.tramut.userInterface.check.CheckInConfirmationScreen
import com.example.tramut.userInterface.check.CheckOutBarcodeScannerScreen
import com.example.tramut.userInterface.check.CheckOutConfirmationScreen
import com.example.tramut.userInterface.check.CheckOutManualEntryScreen
import com.example.tramut.userInterface.check.ManualEntryScreen
import com.example.tramut.userInterface.loginTheme.StaffLoginScreen
import com.example.tramut.userInterface.loginTheme.StudentLoginScreen
import com.example.tramut.userInterface.loginTheme.bottomChooseBar
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.ForgetPasswordScreen1
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.PasswordUpdatedScreen
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.ResetPasswordScreen
import com.example.tramut.userInterface.staffTheme.StaffMenuScreen
import com.example.tramut.userInterface.studentTheme.AvailabilityChartScreen
import com.example.tramut.userInterface.studentTheme.BookingInfoScreen
import com.example.tramut.userInterface.studentTheme.FacilityBookScreen
import com.example.tramut.userInterface.studentTheme.StudentMenuScreen
import com.example.tramut.viewModel.ForgotPwdViewModel
import com.example.tramut.viewModel.LoginViewModel
import com.google.firebase.firestore.FirebaseFirestore


class LoginViewModelFactory(private val usersRepo: UsersRepo): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(usersRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ForgotPwdViewModelFactory(
    private val usersRepo: UsersRepo
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ForgotPwdViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ForgotPwdViewModel(usersRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class AppScreen {
    // Main container
    MainSystem,

    // Tabs
    HomeScreen,
    StudentLoginScreen,
    StaffLoginScreen,
    AdminLoginScreen,

    StudentScreen,
    StaffScreen,
    AdminScreen,

    // Forgot Password Screens
    ForgotPassword1,
    ForgotPassword2,
    ForgotPassword3,
    ResetPwd,
    PwdUpdated,

    // Details under Home tab
    AnnouncementDetail,

    // Student Screens
    StudentBooking,
    StudentBookingDetail,

    // Staff Screens
    StaffCheckBooking,
    StaffApprove,

    // Admin Screens
    AdminMenuScreen,
    AdminCheckin,
    AdminCheckout,
    ViewAn,
    PostAn,
    ViewAnDetail,
    EditAn,
    AddFac,
    EditFac,
    ViewTimetable,
    AdminViewReview,

    // Booking
    CITCBooking,
    LibraryBooking,
    SportsBooking,

    CITCTimetable,
    LibraryTimetable,
    SportsTimetable,
    StudentBookingChart,
    StudentBookingDetails,
    StudentBookingFacility,
    StudentBookingSport,

    //Check-In Screen
    CheckInScanner,
    CheckInManual,
    CheckInSuccess,

    CheckOutScanner,
    CheckOutManual,
    CheckOutSuccess

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScreen(
    currentScreen: AppScreen,
    hasPopBack: () -> Unit,
    selectedTabIndex:Int,
    onTabSelected:(Int) -> Unit
) {
    when(currentScreen) {
        AppScreen.StudentLoginScreen -> {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Student Login",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudentBlue,
                    titleContentColor = Color.White
                )
            )
        }

        AppScreen.StaffLoginScreen -> {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Staff Login",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StaffRed,
                    titleContentColor = Color.White
                )
            )
        }

        AppScreen.AdminLoginScreen -> {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Admin Login",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.StudentBooking ->{
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = hasPopBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(start = 8.dp)

                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Facility Booking",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Blue,
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.StudentBookingChart ->{
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable{hasPopBack()}
                    )
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Availability Chart", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.StudentBookingDetails ->{
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = hasPopBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "Booking Information",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.StudentBookingSport ->{
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
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
                    containerColor = Color(0xFF0D47A1),
                    titleContentColor = Color.White
                )
            )
        }
        else -> {}
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FBSApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startIntent: Intent?,
    usersRepo: UsersRepo
) {
    LaunchedEffect(startIntent) {
        val data = startIntent?.data
        val mode = data?.getQueryParameter("mode")
        val oobCode = data?.getQueryParameter("oobCode")

        if (mode == "resetPassword" && oobCode != null) {
            navController.navigate(
                "${AppScreen.ResetPwd.name}?oobCode=$oobCode"
            ) {
                popUpTo(0)
            }
        }
    }
    val context = LocalContext.current

    // App Database
    val db = remember { AppDatabase.getInstance(context) }
    // Repository
    val usersRepo = remember { UsersRepo(db.usersDao()) }
    // ViewModel
    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(usersRepo)
    )
    val forgotPwdViewModel: ForgotPwdViewModel = viewModel(
        factory = ForgotPwdViewModelFactory(usersRepo)
    )

    val adminsViewModel: AdminsViewModel = viewModel()
    val adminUser = adminsViewModel.adminUser.value
    val currentAdminDept = adminUser?.department ?: "General"
    val currentAdminLoginId = adminUser?.login_id ?: ""

    // Compose 状态
    var password by remember { mutableStateOf("") }


    val studentIdValid by loginViewModel.studentIdValid.collectAsState()
    val staffIdValid by loginViewModel.staffIdValid.collectAsState()
    val idValid by loginViewModel.idValid.collectAsState()
    val studentLoginError by loginViewModel.studentLoginError.collectAsState()
    val staffLoginError by loginViewModel.staffLoginError.collectAsState()
    val showLoginError by loginViewModel.showLoginError.collectAsState()
    val currentUser by loginViewModel.currentUser.collectAsState()
    val isStudentLoggedIn by loginViewModel.isStudentLoggedIn.collectAsState()
    val isStaffLoggedIn by loginViewModel.isStaffLoggedIn.collectAsState()
    val emailError by forgotPwdViewModel.emailError.collectAsState()
    val errorMessage by forgotPwdViewModel.errorMessage.collectAsState()



    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = try {
        AppScreen.valueOf(backStackEntry?.destination?.route ?: "")
    } catch (e: Exception) {
        AppScreen.MainSystem
    }

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopBarScreen(
                currentScreen = currentScreen,
                hasPopBack = { navController.popBackStack() },
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )
        },
        bottomBar = {
            val showBottomBar = currentScreen in listOf(
                AppScreen.HomeScreen,
                AppScreen.StudentLoginScreen,
                AppScreen.StaffLoginScreen,
                AppScreen.AdminLoginScreen,
                AppScreen.StudentScreen,
                AppScreen.StaffScreen,
                AppScreen.AdminScreen,
                AppScreen.AdminLoginScreen,
                AppScreen.StudentScreen,
                AppScreen.StaffScreen,
                AppScreen.AdminMenuScreen
            )

            if (showBottomBar) {
                bottomChooseBar(
                    selectedIndex = when (currentScreen) {
                        AppScreen.HomeScreen -> 0
                        AppScreen.StudentLoginScreen, AppScreen.StudentScreen -> 1
                        AppScreen.StaffLoginScreen, AppScreen.StaffScreen -> 2
                        AppScreen.AdminLoginScreen -> 3
                        else -> 0
                    },
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate(AppScreen.HomeScreen.name)
                            1 -> {
                                if (isStudentLoggedIn) {
                                    navController.navigate(AppScreen.StudentScreen.name)
                                } else {
                                    navController.navigate(AppScreen.StudentLoginScreen.name)
                                }
                            }

                            2 -> {
                                if (isStaffLoggedIn) {
                                    navController.navigate(AppScreen.StaffScreen.name)
                                } else {
                                    navController.navigate(AppScreen.StaffLoginScreen.name)
                                }
                            }
                            3 -> navController.navigate(AppScreen.AdminLoginScreen.name)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppScreen.MainSystem.name,
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            navigation(
                startDestination = AppScreen.HomeScreen.name,
                route = AppScreen.MainSystem.name
            ) {
                composable(route = AppScreen.HomeScreen.name) {
                    HomeScreen(
                        onAnnouncementClick = { announcementId ->
                            navController.navigate("${AppScreen.ViewAnDetail.name}/$announcementId")
                        }
                    )
                }

                // Student Login Screen
                composable(route = AppScreen.StudentLoginScreen.name) {
                    var studentId by rememberSaveable { mutableStateOf("") }
                    var pwd by remember { mutableStateOf(password) }
                    StudentLoginScreen(
                        studentId = studentId.take(7),
                        onStudIdChange = {
                            studentId = it
                            loginViewModel.checkStudentId(it) // <- 分开
                        },
                        idValid = studentIdValid,// <- 分开
                        password = pwd,
                        onPasswordChange = { pwd = it },
                        showLoginError = studentLoginError,// <- 分开
                        onLoginClick = {
                            loginViewModel.login(studentId, pwd, "Student") { success ->
                                if (success) {
                                    navController.navigate(AppScreen.StudentScreen.name) {
                                        popUpTo(AppScreen.StudentLoginScreen.name) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        },
                        onForgotPassClick = {
                            navController.navigate(AppScreen.ForgotPassword1.name)
                        }
                    )
                }

                // Student Main Screen
                composable(route = AppScreen.StudentScreen.name) {
                    var logOutConfirm by rememberSaveable { mutableStateOf(false) }

                    if (logOutConfirm) {
                        AlertDialog(
                            onDismissRequest = { logOutConfirm = false },
                            title = { Text("Confirm Logout") },
                            text = { Text("Are you sure you want to logout?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    logOutConfirm = false
                                    loginViewModel.logout()
                                    navController.navigate(AppScreen.MainSystem.name)
                                }) { Text("Yes") }
                            },
                            dismissButton = {
                                TextButton(onClick = { logOutConfirm = false }) { Text("No") }
                            },
                            properties = DialogProperties(dismissOnClickOutside = false)
                        )
                    }

                    StudentMenuScreen(
                        name = currentUser?.username ?: "",
                        studentId = currentUser?.loginId ?: "",
                        email = currentUser?.email ?: "",
                        onLogoutClick = { logOutConfirm = true },
                        onMyBookingClick = {
                            navController.navigate(AppScreen.StudentBookingDetail.name)
                        },
                        onFacilityBookingClick = {
                            navController.navigate(AppScreen.StudentBooking.name)
                        },
                        onFeedbackClick = {
                            navController.navigate(AppScreen.StudentBooking.name)
                        },
                        onSettingsClick = {
                            navController.navigate(AppScreen.StudentBooking.name)
                        }
                    )
                }
                // Staff Login Screen
                composable(route = AppScreen.StaffLoginScreen.name) {
                    var staffId by rememberSaveable { mutableStateOf("") }
                    var pwd by remember { mutableStateOf(password) }
                    StaffLoginScreen(
                        staffId = staffId.take(4),
                        onStaffIdChange = {
                            staffId = it
                            loginViewModel.checkStaffId(it) // <- 分开
                        },
                        idValid = staffIdValid,
                        password = pwd,
                        onPasswordChange = { pwd = it },
                        showLoginError = staffLoginError, // <- 分开
                        onLoginClick = {
                            loginViewModel.login(staffId, pwd, "Staff") { success ->
                                if (success) {
                                    navController.navigate(AppScreen.StaffScreen.name) {
                                        popUpTo(AppScreen.StaffLoginScreen.name) {
                                            inclusive = true
                                        }
                                    }
                                }
                            }
                        },
                        onForgotPassClick = {
                            navController.navigate(AppScreen.ForgotPassword1.name)
                        }
                    )
                }
                // Staff Main Screen
                composable(route = AppScreen.StaffScreen.name) {
                    var logOutConfirm by rememberSaveable { mutableStateOf(false) }

                    if (logOutConfirm) {
                        AlertDialog(
                            onDismissRequest = { logOutConfirm = false },
                            title = { Text("Confirm Logout") },
                            text = { Text("Are you sure you want to logout?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    logOutConfirm = false
                                    loginViewModel.logout()
                                    navController.navigate(AppScreen.MainSystem.name)
                                }) { Text("Yes") }
                            },
                            dismissButton = {
                                TextButton(onClick = { logOutConfirm = false }) { Text("No") }
                            },
                            properties = DialogProperties(dismissOnClickOutside = false)
                        )
                    }

                    StaffMenuScreen(
                        name = currentUser?.username ?: "",
                        staffId = currentUser?.loginId ?: "",
                        email = currentUser?.email ?: "",
                        onLogoutClick = { logOutConfirm = true },
                        onMyBookingClick = {
                            navController.navigate(AppScreen.StudentBookingDetail.name)
                        },
                        onFacilityBookingClick = {
                            navController.navigate(AppScreen.StudentBooking.name)
                        },
                        onFeedbackClick = {
                            navController.navigate(AppScreen.StudentBooking.name)
                        },
                        onSettingsClick = {
                            navController.navigate(AppScreen.StudentBooking.name)
                        }
                    )

                }

                // 1. ADMIN LOGIN
                composable(route = AppScreen.AdminLoginScreen.name) {
                    var adminId by rememberSaveable { mutableStateOf("") }
                    var pwd by remember { mutableStateOf(password) }
                    AdminLoginScreen(
                        viewModel = adminsViewModel,
                        onLoginSuccess = {
                            navController.navigate(AppScreen.AdminMenuScreen.name) {
                                popUpTo(AppScreen.AdminLoginScreen.name) { inclusive = true }
                            }
                        }
                    )
                }

                // 2. ADMIN DASHBOARD
                composable(route = AppScreen.AdminMenuScreen.name) {
                    AdminMainScreen(
                        navController = navController,
                        viewModel = adminsViewModel,
                        onClick = {
                            navController.navigate(AppScreen.CheckInScanner.name) {
                                popUpTo(AppScreen.AdminMenuScreen.name) { inclusive = true }
                            }
                        }
                    )
                }

                // 3. ANNOUNCEMENTS LIST
                composable(route = AppScreen.ViewAn.name) {
                    AdminAnnouncementScreen(
                        currentAdminDepartment = currentAdminDept,
                        onNavigateToEdit = { docId -> navController.navigate("${AppScreen.EditAn.name}/$docId") },
                        onNavigateToAdd = { navController.navigate(AppScreen.PostAn.name) },
                        onNavigateToDetail = { id ->
                            navController.navigate("${AppScreen.ViewAnDetail.name}/$id")
                        },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = "${AppScreen.ViewAnDetail.name}/{anId}",
                    arguments = listOf(navArgument("anId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val anId = backStackEntry.arguments?.getString("anId") ?: ""
                    AnnouncementDetailScreen(
                        announcementId = anId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 4. POST ANNOUNCEMENT
                composable(route = AppScreen.PostAn.name) {
                    PostAnnouncementScreen(
                        adminDepartment = currentAdminDept,
                        adminId = currentAdminLoginId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 5. EDIT ANNOUNCEMENT (Dynamic ID)
                composable(
                    route = "${AppScreen.EditAn.name}/{announcementId}",
                    arguments = listOf(navArgument("announcementId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("announcementId") ?: ""
                    EditAnnouncementScreen(
                        announcementId = id,
                        adminDepartment = currentAdminDept,
                        onUpdateSuccess = { navController.popBackStack() },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }



                // 6. ADD FACILITY
                composable(route = AppScreen.AddFac.name) {
                    AdminAddFacilityScreen(
                        adminDepartment = currentAdminDept,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 7. EDIT FACILITY
                composable(route = AppScreen.EditFac.name) {
                    EditFacilityScreen(
                        adminDepartment = currentAdminDept,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // 8. preview tt
                composable(
                    route = "${AppScreen.ViewTimetable.name}/{departmentName}",
                    arguments = listOf(navArgument("departmentName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val departmentName = backStackEntry.arguments?.getString("departmentName") ?: "Sport"

                    // Pass to 'initialDepartment'
                    TimetableScreen(
                        initialDepartment = departmentName,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                // Forgot Password Screen
                composable(route = AppScreen.ForgotPassword1.name) {
                    var email by rememberSaveable { mutableStateOf("") }
                    var ic by rememberSaveable { mutableStateOf("") }
                    ForgetPasswordScreen1(
                        emailInput = email,
                        icInput = ic,
                        errorMessage = emailError,
                        onEmailInputChange = {
                            email = it
                        },
                        onIcInputChange = {
                            ic = it
                        },
                        onCancelForgetPwdClick = {
                            navController.navigate(AppScreen.MainSystem.name)
                        },
                        onRequestPwdResetClick = {
                            forgotPwdViewModel.requestPasswordReset(email, ic) {
                                navController.navigate("${AppScreen.ResetPwd.name}?oobCode={oobCode}") {
                                    popUpTo(AppScreen.MainSystem.name)
                                    { inclusive = true }
                                }
                            }
                        }
                    )
                }


                composable(
                    route = "${AppScreen.ResetPwd.name}?oobCode={oobCode}",
                    arguments = listOf(navArgument("oobCode") { type = NavType.StringType })
                ) { backStackEntry ->
                    var newPassword by rememberSaveable { mutableStateOf("") }
                    var confirmPassword by rememberSaveable { mutableStateOf("") }

                    val oobCode = backStackEntry.arguments?.getString("oobCode")
                    if (oobCode == null) {
                        Text("Invalid or expired reset link")
                        return@composable
                    }

                    val ruleMinLength = forgotPwdViewModel.hasMinLength(newPassword)
                    val ruleLower = forgotPwdViewModel.hasLowerCase(newPassword)
                    val ruleUpper = forgotPwdViewModel.hasUpperCase(newPassword)
                    val ruleNumberSpecial = forgotPwdViewModel.hasNumberOrSpecial(newPassword)


                    ResetPasswordScreen(
                        newPassword = newPassword,
                        onNewPasswordChange = {
                            newPassword = it
                                              },
                        ruleMinLength = ruleMinLength,
                        ruleLower = ruleLower,
                        ruleUpper = ruleUpper,
                        ruleNumberSpecial = ruleNumberSpecial,
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = {
                            confirmPassword = it
                                                  },
                        onSubmitClick = {
                            // 调用 ViewModel 方法重置密码
                            forgotPwdViewModel.resetPassword(oobCode, newPassword) {
                                navController.navigate(AppScreen.PwdUpdated.name)
                            }
                        },
                        onCancelClick = {
                            navController.navigate(AppScreen.HomeScreen.name)
                        }
                    )
                }

                composable(route = AppScreen.PwdUpdated.name){
                    PasswordUpdatedScreen(
                        onStudentLoginClick = {
                            navController.navigate(AppScreen.StudentLoginScreen.name){
                                popUpTo(AppScreen.PwdUpdated.name)
                                { inclusive = true }
                            }
                        },
                        onStaffLoginClick = {
                            navController.navigate(AppScreen.StaffLoginScreen.name){
                                popUpTo(AppScreen.PwdUpdated.name)
                                { inclusive = true }
                            }
                        }
                    )
                }


                composable(route = AppScreen.StudentBooking.name) {

                    FacilityBookScreen(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        navController = navController,
                        userId = currentUser?.loginId ?: "",

                        onCITCABClick = {
                            navController.navigate("${AppScreen.StudentBookingSport.name}/Cyber Centre Discussion Room/")
                        },
                        onLibraryABClick = {
                            navController.navigate("${AppScreen.StudentBookingSport.name}/Library Discussion Room/")
                        },
                        onSportsABClick = {
                            navController.navigate("${AppScreen.StudentBookingSport.name}/Sports Facilities/")
                        },

                        onCITCTTClick = {
                            navController.navigate(AppScreen.CITCTimetable.name)
                        },
                        onLibraryTTClick = {
                            navController.navigate(AppScreen.LibraryTimetable.name)
                        },
                        onSportsTTClick = {
                            navController.navigate(AppScreen.SportsTimetable.name)
                        }
                    )
                }

                composable(route = AppScreen.SportsTimetable.name) {
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Sports Facilities",
                        onBookNow = { selectedVenue, selectedDate ->
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$selectedVenue/$selectedDate")
                        }
                    )
                }

                composable(
                    route = "BookingInfo/{bookingId}"
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    var booking by remember { mutableStateOf<Booking?>(null) }

                    LaunchedEffect(bookingId) {
                        FirebaseFirestore.getInstance()
                            .collection("sportBookings")
                            .document(bookingId)
                            .get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    booking = Booking(
                                        facility = doc.getString("facility") ?: "",
                                        bookingNo = doc.getString("bookingId") ?: doc.id,
                                        date = doc.getString("date") ?: "",
                                        duration = doc.getString("duration") ?: "",
                                        venue = doc.getString("venue") ?: "",
                                        level = doc.getString("level") ?: "",
                                        building = doc.getString("building") ?: "",
                                        checkIn = doc.getString("checkIn") ?: "",
                                        checkOut = doc.getString("checkOut") ?: "",
                                        status = doc.getString("status") ?: "Booked"
                                    )
                                }
                            }
                    }

                    booking?.let { BookingInfoScreen(it) }
                }


                composable(route = AppScreen.CITCBooking.name) {
                    // CITC Booking Screen
                }
                composable(route = AppScreen.LibraryBooking.name) {
                    // Library Booking Screen
                }
                composable(route = AppScreen.SportsBooking.name) {
                    // Sports Booking Screen
                }
                composable(route = AppScreen.CITCTimetable.name) {
                    // CITC Timetable
                }
                composable(route = AppScreen.LibraryTimetable.name) {
                    // Library Timetable
                }

                composable(
                    route = AppScreen.CheckInScanner.name
                ) { backStackEntry ->
                    BarcodeScannerScreen(
                        onScanSuccess = { scannedId ->
                            // Handle the scan logic here or pass to a VM
                            // For now, we assume scan is valid and move to success
                            navController.navigate("${AppScreen.CheckInManual.name}/$scannedId")
                        },
                        onManualInputClicked = {
                            // Navigate to Manual Entry, passing the bookingId
                            navController.navigate("${AppScreen.CheckInManual.name}/_empty_")
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                // 2. Manual Entry Screen
                composable(
                    route = "${AppScreen.CheckInManual.name}/{bookingId}"
                ) { backStackEntry ->
                    // If the ID is "_empty_", we pass an empty string to the screen
                    val arg = backStackEntry.arguments?.getString("bookingId") ?: ""
                    val bookingId = if (arg == "_empty_") "" else arg

                    ManualEntryScreen(
                        bookingId = bookingId,
                        isCheckIn = true,
                        initialId = bookingId,
                        onSuccess = {
                            navController.navigate(AppScreen.CheckInSuccess.name) {
                                popUpTo(AppScreen.CheckInScanner.name) { inclusive = true }
                            }
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                // 3. Success Confirmation Screen
                composable(route = AppScreen.CheckInSuccess.name) {
                    CheckInConfirmationScreen(
                        onOkClicked = {
                            // Navigate back to Home or Booking List
                            navController.navigate(AppScreen.AdminMenuScreen.name) {
                                popUpTo(AppScreen.AdminMenuScreen.name) { inclusive = true }
                            }
                        },
                        onBackClicked = {
                            // Optional: Define where the back arrow goes (or hide it in the screen logic)
                            navController.navigate(AppScreen.AdminMenuScreen.name)
                        }
                    )
                }

                composable(
                    route = AppScreen.CheckOutScanner.name
                ) { backStackEntry ->
                    CheckOutBarcodeScannerScreen(
                        onScanSuccess = { scannedId ->

                            navController.navigate("${AppScreen.CheckOutManual.name}/$scannedId")
                        },
                        onManualInputClicked = {
                            navController.navigate("${AppScreen.CheckOutManual.name}/_empty_")
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "${AppScreen.CheckOutManual.name}/{bookingId}"
                ) { backStackEntry ->
                    // If the ID is "_empty_", we pass an empty string to the screen
                    val arg = backStackEntry.arguments?.getString("bookingId") ?: ""
                    val bookingId = if (arg == "_empty_") "" else arg

                    CheckOutManualEntryScreen(
                        bookingId = bookingId,
                        isCheckIn = false,
                        initialId = bookingId,
                        onSuccess = {
                            navController.navigate(AppScreen.CheckOutSuccess.name) {
                                popUpTo(AppScreen.CheckOutScanner.name) { inclusive = true }
                            }
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(route = AppScreen.CheckOutSuccess.name) {
                    CheckOutConfirmationScreen(
                        onOkClicked = {
                            // Navigate back to Home or Booking List
                            navController.navigate(AppScreen.AdminMenuScreen.name) {
                                popUpTo(AppScreen.AdminMenuScreen.name) { inclusive = true }
                            }
                        },
                        onBackClicked = {
                            // Optional: Define where the back arrow goes (or hide it in the screen logic)
                            navController.navigate(AppScreen.AdminMenuScreen.name)
                        }
                    )
                }
            }
        }
    }
}
