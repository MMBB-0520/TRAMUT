package com.example.tramut

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults.containerColor
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.myfacilitybookingsystem.userInterface.adminTheme.AdminMainScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.AdminAnnouncementScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.AnnouncementDetailScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.EditAnnouncementScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Announcement.PostAnnouncementScreen
import com.example.myfacilitybookingsystem.userInterface.adminTheme.Facility.AdminAddFacilityScreen
import com.example.myfacilitybookingsystem.viewModel.AdminsViewModel
import com.example.tramut.rooms.entity.Booking
import com.example.tramut.rooms.entity.Member
import com.example.tramut.rooms.repo.UsersRepo
import com.example.tramut.ui.theme.StaffRed
import com.example.tramut.ui.theme.StudentBlue
import com.example.tramut.userInterface.HomeScreen
import com.example.tramut.userInterface.TimetableScreen
import com.example.tramut.userInterface.adminTheme.AdminReviewScreen
import com.example.tramut.userInterface.adminTheme.Facility.EditFacilityScreen
import com.example.tramut.userInterface.adminTheme.AdminReviewScreen
import com.example.tramut.userInterface.loginTheme.AdminLoginScreen
import com.example.tramut.userInterface.check.CheckInConfirmationScreen
import com.example.tramut.userInterface.check.CheckOutBarcodeScannerScreen
import com.example.tramut.userInterface.check.CheckOutConfirmationScreen
import com.example.tramut.userInterface.check.CheckOutManualEntryScreen
import com.example.tramut.userInterface.check.ManualEntryScreen
import com.example.tramut.userInterface.loginTheme.AdminLoginScreen
import com.example.tramut.userInterface.loginTheme.StaffLoginScreen
import com.example.tramut.userInterface.loginTheme.StudentLoginScreen
import com.example.tramut.userInterface.loginTheme.bottomChooseBar
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.ForgetPasswordScreen1
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.ForgetPasswordScreen3
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.PasswordUpdatedScreen
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.ResetPasswordScreen
import com.example.tramut.userInterface.settings.AboutAppScreen
import com.example.tramut.userInterface.settings.ChangePasswordScreen
import com.example.tramut.userInterface.settings.PrivacyPolicyScreen
import com.example.tramut.userInterface.settings.SettingsScreen
import com.example.tramut.userInterface.settings.ThemeSelectionScreen
import com.example.tramut.userInterface.settings.ThemeViewModel
import com.example.tramut.userInterface.studentTheme.AvailabilityChartScreen
import com.example.tramut.userInterface.studentTheme.BookSportScreen
import com.example.tramut.userInterface.studentTheme.BookingInfoScreen
import com.example.tramut.userInterface.studentTheme.FacilityBookScreen
import com.example.tramut.userInterface.studentTheme.MyBookingScreen
import com.example.tramut.userInterface.studentTheme.ReviewScreen
import com.example.tramut.userInterface.studentTheme.ReviewSubmissionScreen
import com.example.tramut.userInterface.studentTheme.UserMenuScreen
import com.example.tramut.userInterface.studentTheme.getContainerColor
import com.example.tramut.viewModel.ForgotPwdViewModel
import com.example.tramut.viewModel.LoginViewModel
import com.example.tramut.viewModel.MyBookingViewModel
import com.example.tramut.viewModel.ReviewViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.collections.map

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

    UserScreen,

    UserSetting,
    AboutApp,
    ChangePwd,
    Theme,
    Privacy,

    // Forgot Password Screens
    ForgotPassword1,
    ForgotPassword2,
    ForgotPassword3,
    ResetPwd,
    PwdUpdated,

    UserReview,
    ReviewSubmission,
    AdminViewReview,

    // Details under Home tab
    AnnouncementDetail,

    // Student Screens
    StudentBooking,

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

    // Booking
    CITCBooking,
    LibraryBooking,
    SportsBooking,

    CITCTimetable,
    LibraryTimetable,
    SportsTimetable,
    StudentBookingChart,
    StudentMyBooking,
    StudentBookingDetails,
    StudentBookingFacility,
    StudentBookingSport,
    StudentAvailabilityChart,

    CheckInSuccess,
    CheckOutSuccess,
    CheckOutManual,
    CheckOutScanner,
    CheckInManual,
    CheckInScanner,
    CheckInConfirmation,
    CheckOutConfirmation

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarScreen(
    currentScreen: AppScreen,
    hasPopBack: () -> Unit,
    addReview: () -> Unit,
    containerColor: Color,
    selectedTabIndex: Int
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
                    containerColor = containerColor,
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.StudentBooking -> {
            val title = when (selectedTabIndex) {
                1 -> "My Bookings"
                else -> "Facility Booking"
            }

            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = hasPopBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor
                )
            )
        }
        AppScreen.CITCTimetable,
        AppScreen.LibraryTimetable,
        AppScreen.SportsTimetable-> {
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
                    containerColor = containerColor,
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.StudentBookingSport -> {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
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
                    containerColor = containerColor,
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.UserSetting -> {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
                    )
                },
                title = {
                    Box {
                        Text(
                            text = "    Settings",
                            fontSize = 24.sp
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF512DA8),
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.ChangePwd -> {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
                    )
                },
                title = {
                    Box {
                        Text(
                            text = "    Change Password",
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF512DA8),
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.AboutApp -> {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
                    )
                },
                title = {
                    Box {
                        Text(
                            text = "    App Info",
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF512DA8),
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.Privacy -> {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
                    )
                },
                title = {
                    Box {
                        Text(
                            text = "    Privacy Policy",
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF512DA8),
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.UserReview -> {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Review",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
                    )
                },
                actions = {
                    IconButton(onClick = { addReview() } ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = Color.White
                )
            )
        }
        AppScreen.ReviewSubmission -> {
            TopAppBar(
                navigationIcon = {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp).clickable { hasPopBack() }
                    )
                },
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Review",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 24.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
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
    themeViewModel: ThemeViewModel
) {

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
    val reviewViewModel: ReviewViewModel = viewModel()
    val bookingViewModel: MyBookingViewModel = viewModel()

    val adminsViewModel: AdminsViewModel = viewModel()
    val adminUser = adminsViewModel.adminUser.value
    val currentAdminDept = adminUser?.department ?: "General"
    val currentAdminLoginId = adminUser?.login_id ?: ""

    var password by remember { mutableStateOf("") }

    val studentIdValid by loginViewModel.studentIdValid.collectAsState()
    val staffIdValid by loginViewModel.staffIdValid.collectAsState()
    val studentLoginError by loginViewModel.studentLoginError.collectAsState()
    val staffLoginError by loginViewModel.staffLoginError.collectAsState()
    val currentUser by loginViewModel.currentUser.collectAsState()
    val isStudentLoggedIn by loginViewModel.isStudentLoggedIn.collectAsState()
    val isStaffLoggedIn by loginViewModel.isStaffLoggedIn.collectAsState()
    val emailError by forgotPwdViewModel.emailError.collectAsState()
    val lastRequestedEmail by forgotPwdViewModel.lastRequestedEmail.collectAsState()
    val errorMessage by forgotPwdViewModel.errorMessage.collectAsState()
    val reviews by reviewViewModel.reviews.collectAsState()
    val bookings by reviewViewModel.bookings.collectAsState()
    val containerColor = getContainerColor(isStudentLoggedIn, isStaffLoggedIn)
    val selectedTabIndex by bookingViewModel.selectedTabIndex.collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = try {
        AppScreen.valueOf(backStackEntry?.destination?.route ?: "")
    } catch (e: Exception) {
        AppScreen.MainSystem
    }

    Scaffold(
        topBar = {
            TopBarScreen(
                currentScreen = currentScreen,
                hasPopBack = { navController.popBackStack() },
                addReview = { navController.navigate(AppScreen.ReviewSubmission.name) },
                containerColor = containerColor,
                selectedTabIndex = selectedTabIndex
            )
        },
        bottomBar = {
            val showBottomBar = currentScreen in listOf(
                AppScreen.HomeScreen,
                AppScreen.StudentLoginScreen,
                AppScreen.StaffLoginScreen,
                AppScreen.AdminLoginScreen,
                AppScreen.UserScreen,

                )

            if (showBottomBar) {
                bottomChooseBar(
                    selectedIndex = when (currentScreen) {
                        AppScreen.HomeScreen -> 0

                        AppScreen.StudentLoginScreen -> 1

                        AppScreen.StaffLoginScreen -> 2

                        AppScreen.UserScreen -> {
                            when {
                                isStudentLoggedIn -> 1
                                isStaffLoggedIn -> 2
                                else -> 0
                            }
                        }

                        AppScreen.AdminLoginScreen -> 3

                        else -> 0
                    },
                    onItemSelected = { index ->
                        when (index) {
                            0 -> navController.navigate(AppScreen.HomeScreen.name)
                            1 -> {
                                if (isStudentLoggedIn) {
                                    navController.navigate(AppScreen.UserScreen.name)
                                } else {
                                    navController.navigate(AppScreen.StudentLoginScreen.name)
                                }
                            }

                            2 -> {
                                if (isStaffLoggedIn) {
                                    navController.navigate(AppScreen.UserScreen.name)
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
                            loginViewModel.checkStudentId(it)
                        },
                        idValid = studentIdValid,
                        password = pwd,
                        onPasswordChange = { pwd = it },
                        showLoginError = studentLoginError,
                        onLoginClick = {
                            loginViewModel.login(studentId, pwd, "Student") { success ->
                                if (success) {
                                    navController.navigate(AppScreen.UserScreen.name) {
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


                composable(route = AppScreen.UserSetting.name) {
                    SettingsScreen(
                        onChangePasswordClick = {
                            navController.navigate(AppScreen.ChangePwd.name)
                        },
                        onThemeClick = {
                            navController.navigate(AppScreen.Theme.name)
                        },
                        onPrivacyClick = {
                            navController.navigate(AppScreen.Privacy.name)
                        },
                        onAboutClick = {
                            navController.navigate(AppScreen.AboutApp.name)
                        }
                    )
                }

                composable(route = AppScreen.Theme.name) {
                    ThemeSelectionScreen(
                        viewModel = themeViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(route = AppScreen.AboutApp.name) {
                    AboutAppScreen()
                }
                composable(route = AppScreen.Privacy.name) {
                    PrivacyPolicyScreen()
                }
                composable(route = AppScreen.ChangePwd.name) {
                    var newPassword by rememberSaveable { mutableStateOf("") }
                    var confirmPassword by rememberSaveable { mutableStateOf("") }
                    var oldPassword by rememberSaveable { mutableStateOf("") }
                    val ruleMinLength = forgotPwdViewModel.hasMinLength(newPassword)
                    val ruleLower = forgotPwdViewModel.hasLowerCase(newPassword)
                    val ruleUpper = forgotPwdViewModel.hasUpperCase(newPassword)
                    val ruleNumberSpecial = forgotPwdViewModel.hasNumberOrSpecial(newPassword)


                    ChangePasswordScreen(
                        errorMsg = errorMessage,
                        oldPassword = oldPassword,
                        onOldPasswordChange = {
                            oldPassword = it
                        },
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
                        onChangePasswordClick = {
                            forgotPwdViewModel.changePassword(oldPassword, newPassword) {
                                navController.navigate(AppScreen.PwdUpdated.name) {
                                    popUpTo(AppScreen.ChangePwd.name)
                                    { inclusive = true }
                                }
                            }
                        },
                        onCancelClick = {
                            navController.popBackStack()
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
                            loginViewModel.checkStaffId(it)
                        },
                        idValid = staffIdValid,
                        password = pwd,
                        onPasswordChange = { pwd = it },
                        showLoginError = staffLoginError,
                        onLoginClick = {
                            loginViewModel.login(staffId, pwd, "Staff") { success ->
                                if (success) {
                                    navController.navigate(AppScreen.UserScreen.name) {
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
                // User Main Screen
                composable(route = AppScreen.UserScreen.name) {
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

                    UserMenuScreen(
                        containColor = containerColor,
                        name = currentUser?.username ?: "",
                        loginId = currentUser?.loginId ?: "",
                        email = currentUser?.email ?: "",
                        onLogoutClick = { logOutConfirm = true },
                        onMyBookingClick = {
                            bookingViewModel.setTab(1)
                            navController.navigate(AppScreen.StudentBooking.name)

                                           },
                        onFacilityBookingClick = {
                            bookingViewModel.setTab(0)
                            navController.navigate(AppScreen.StudentBooking.name)
                        },
                        onFeedbackClick = {
                            navController.navigate(AppScreen.UserReview.name)
                        },
                        onSettingsClick = {
                            navController.navigate(AppScreen.UserSetting.name)
                        }
                    )

                }

                // 1. ADMIN LOGIN
                composable(route = AppScreen.AdminLoginScreen.name) {

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
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToBooking = { facilityId, hour, date ->
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$facilityId/$date/$hour")
                        }
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
                                navController.navigate(AppScreen.ForgotPassword3.name) {
                                    popUpTo(AppScreen.ForgotPassword1.name)
                                    { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(route = AppScreen.ForgotPassword3.name) {
                    var oobCode by rememberSaveable { mutableStateOf("") }

                    ForgetPasswordScreen3(
                        email = lastRequestedEmail,
                        oobCode = oobCode,
                        onOobCodeChange = {
                            oobCode = it
                        },
                        onResendCodeClick = {
                            forgotPwdViewModel.resendResetEmail()
                        },
                        onContinueResetClick = {
                            navController.navigate("${AppScreen.ResetPwd.name}?oobCode=${Uri.encode(oobCode)}") {
                                popUpTo(AppScreen.ForgotPassword3.name)
                                { inclusive = true }
                            }
                        },
                        onChangeEmailClick = {
                            navController.navigate(AppScreen.ForgotPassword1.name) {
                                popUpTo(AppScreen.ForgotPassword3.name)
                                { inclusive = true }
                            }
                        }
                    )
                }


                composable(
                    route = "${AppScreen.ResetPwd.name}?oobCode={oobCode}",
                    arguments = listOf(
                        navArgument("oobCode") {
                            type = NavType.StringType
                            nullable = false
                        }
                    )
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
                            forgotPwdViewModel.resetPassword(oobCode, newPassword) {
                                navController.navigate(AppScreen.PwdUpdated.name) {
                                    popUpTo(AppScreen.ResetPwd.name)
                                    { inclusive = true }
                                }
                            }
                        },
                        onCancelClick = {
                            navController.navigate(AppScreen.HomeScreen.name) {
                                popUpTo(AppScreen.ResetPwd.name)
                                { inclusive = true }
                            }
                        }
                    )
                }

                composable(route = AppScreen.PwdUpdated.name) {
                    PasswordUpdatedScreen(
                        onStudentLoginClick = {
                            navController.navigate(AppScreen.StudentLoginScreen.name) {
                                popUpTo(AppScreen.PwdUpdated.name)
                                { inclusive = true }
                            }
                        },
                        onStaffLoginClick = {
                            navController.navigate(AppScreen.StaffLoginScreen.name) {
                                popUpTo(AppScreen.PwdUpdated.name)
                                { inclusive = true }
                            }
                        }
                    )
                }

                composable(route = AppScreen.StudentBooking.name) {
                    FacilityBookScreen(
                        selectedTabIndex = selectedTabIndex,
                        onTab0Selected = { bookingViewModel.setTab(0) },
                        onTab1Selected = { bookingViewModel.setTab(1) },
                        navController = navController,
                        userId = currentUser?.loginId ?: "",

                        onCITCABClick = {
                            val today = java.time.LocalDate.now().toString()
                            navController.navigate("${AppScreen.StudentBookingSport.name}/Cyber Centre/Select Venue/$today")
                        },
                        onLibraryABClick = {
                            val today = java.time.LocalDate.now().toString()
                            navController.navigate("${AppScreen.StudentBookingSport.name}/Library/Select Venue/$today")
                        },
                        onSportsABClick = {
                            val today = java.time.LocalDate.now().toString()
                            navController.navigate("${AppScreen.StudentBookingSport.name}/Sport Facilities/Select Venue/$today")
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

                composable(
                    route = "${AppScreen.StudentBookingDetails.name}/{bookingId}",
                    arguments = listOf(navArgument("bookingId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    var booking by remember { mutableStateOf<Booking?>(null) }

                    LaunchedEffect(bookingId) {
                        FirebaseFirestore.getInstance()
                            .collection("bookings")
                            .document(bookingId)
                            .get()
                            .addOnSuccessListener { doc ->
                                if (doc.exists()) {
                                    // 使用 toObject 自动转换整个对象
                                    val b = doc.toObject(Booking::class.java)?.copy(
                                        // 确保 bookingId 和 bookingNo 正确（如果文档 ID 就是编号）
                                        bookingId = doc.id,
                                        bookingNo = doc.getString("bookingNo") ?: doc.id
                                    )
                                    booking = b
                                }
                            }
                    }

                    booking?.let {
                        BookingInfoScreen(
                            booking = it,
                            containerColor = containerColor,
                            navController = navController
                        )
                    }
                }

                composable(route = AppScreen.StudentBookingSport.name + "/{facilityType}/{venue}/{date}",
                    arguments = listOf(
                        navArgument("facilityType") { type = NavType.StringType },
                        navArgument("venue") { type = NavType.StringType },
                        navArgument("date") { type = NavType.StringType }
                    )
                ) { backStackEntry ->

                    val venue = backStackEntry.arguments?.getString("venue") ?: ""
                    val date = backStackEntry.arguments?.getString("date") ?: ""
                    val facilityType = backStackEntry.arguments?.getString("facilityType") ?: "Sport Facilities"

                    val isStaffLoggedIn by loginViewModel.isStaffLoggedIn.collectAsState()

                    BookSportScreen(
                        facilityType = facilityType,
                        selectedDateFromPrevious = date,
                        selectedVenueFromPrevious = venue,
                        onBackFacilityPage = {
                            navController.navigate(AppScreen.StudentBooking.name) {
                                popUpTo(AppScreen.StudentBooking.name) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onSubmit = { venueType, date, startTime, endTime, pax, members, lvl, build ->
                            val bookingId = UUID.randomUUID().toString()
                            val bookingData = hashMapOf(
                                "bookingId" to bookingId,
                                "userId" to currentUser?.loginId,
                                "facility" to facilityType,
                                "venue" to venueType,
                                "date" to date,
                                "startTime" to startTime,
                                "endTime" to endTime,
                                "duration" to "$startTime - $endTime",
                                "pax" to pax,
                                "members" to members.map { it.id to it.name },
                                "status" to "Booked",
                                "level" to lvl,
                                "building" to build
                            )

                            FirebaseFirestore.getInstance()
                                .collection("bookings")
                                .document(bookingId)
                                .set(bookingData)
                                .addOnSuccessListener {
                                    navController.navigate(AppScreen.StudentBooking.name) {
                                        // Clear the back stack so user can't go back to booking form
                                        popUpTo(AppScreen.StudentBooking.name) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("Firebase", "Failed to save booking", it)
                                }
                        },
                        isStaff = isStaffLoggedIn,
                        userRepository = usersRepo
                    )
                }




                composable(route = AppScreen.CITCBooking.name) {
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Cyber Centre Discussion Room",
                        onBookNow = { selectedVenue, selectedDate ->
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$selectedVenue/$selectedDate")
                        }
                    )// CITC Booking Screen
                }
                composable(route = AppScreen.LibraryBooking.name) {
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Library Discussion Room",
                        onBookNow = { selectedVenue, selectedDate ->
                            navController.navigate("${AppScreen. StudentBookingSport.name}/$selectedVenue/$selectedDate")
                        }
                    )
                    // Library Booking Screen
                }
                composable(route = AppScreen.SportsBooking.name) {
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Sports Facilities",
                        onBookNow = { selectedVenue, selectedDate ->
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$selectedVenue/$selectedDate")
                        }
                    )
                    // Sports Booking Screen
                }

                composable(route = AppScreen.CITCTimetable.name) {
                    val facilityType = "Cyber Centre"
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Cyber Centre Discussion Room",
                        onBookNow = { selectedVenue, selectedDate ->
                            val encodedVenue = URLEncoder.encode(selectedVenue, StandardCharsets.UTF_8.toString())
                            val encodedDate = URLEncoder.encode(selectedDate, StandardCharsets.UTF_8.toString())
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$facilityType/$encodedVenue/$encodedDate")
                        }
                    )
                }

                composable(route = AppScreen.LibraryTimetable.name) {
                    val facilityType = "Library"
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Library Discussion Room",
                        onBookNow = { selectedVenue, selectedDate ->
                            val encodedVenue = URLEncoder.encode(selectedVenue, StandardCharsets.UTF_8.toString())
                            val encodedDate = URLEncoder.encode(selectedDate, StandardCharsets.UTF_8.toString())
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$facilityType/$encodedVenue/$encodedDate")
                        }
                    )
                }


                composable(route = AppScreen.SportsTimetable.name) {
                    val facilityType = "Sport"
                    AvailabilityChartScreen(
                        selectedFacilityFromPrevious = "Sports Facilities",
                        onBookNow = { selectedVenue, selectedDate ->
                            val encodedVenue = URLEncoder.encode(selectedVenue, StandardCharsets.UTF_8.toString())
                            val encodedDate = URLEncoder.encode(selectedDate, StandardCharsets.UTF_8.toString())
                            navController.navigate("${AppScreen.StudentBookingSport.name}/$facilityType/$encodedVenue/$encodedDate")
                        }
                    )
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
                            navController.navigate("${AppScreen.CheckInManual.name}/empty")
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
                    // If the ID is "empty", we pass an empty string to the screen
                    val arg = backStackEntry.arguments?.getString("bookingId") ?: ""
                    val bookingId = if (arg == "empty") "" else arg

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
                            // Navigate to Manual Entry, passing the bookingId
                            navController.navigate("${AppScreen.CheckOutManual.name}/empty")
                        },
                        onBackClicked = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = "${AppScreen.CheckOutManual.name}/{bookingId}"
                ) { backStackEntry ->
                    // If the ID is "empty", we pass an empty string to the screen
                    val arg = backStackEntry.arguments?.getString("bookingId") ?: ""
                    val bookingId = if (arg == "empty") "" else arg

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

                composable(route = AppScreen.UserReview.name) {
                    LaunchedEffect(Unit) {
                        reviewViewModel.fetchMyReviews(currentUser?.loginId)
                    }
                    var selectedTab by remember { mutableStateOf("All") }
                    val filteredReviews = reviewViewModel.filterByStatus(reviews, selectedTab)
                    ReviewScreen(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        filteredReviews = filteredReviews,
                        containColor = containerColor
                    )
                }

                composable(route = AppScreen.ReviewSubmission.name) {
                    LaunchedEffect(Unit) {
                        reviewViewModel.fetchMyBookings(currentUser?.loginId)
                    }
                    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
                    var selectedCategory by remember { mutableStateOf<String?>(null) }
                    var comment by remember { mutableStateOf("") }
                    val submitSuccess by reviewViewModel.submitSuccess.collectAsState()

                    ReviewSubmissionScreen(
                        containerColor = containerColor,
                        bookings = bookings,
                        selectedBooking = selectedBooking,
                        onBookingSelected = {
                            selectedBooking = it
                        },
                        selectedCategory = selectedCategory,
                        onCategorySelected = {
                            selectedCategory = it
                        },
                        comment = comment,
                        onCommentChange = {
                            comment = it
                        },
                        onSubmitReviewClick = {
                            reviewViewModel.submitReview(
                                userId = currentUser?.loginId,
                                booking = selectedBooking!!,
                                category = selectedCategory!!,
                                description = comment
                            )
                        },
                        submitSuccess = submitSuccess,
                        onOk = {
                            reviewViewModel.resetSubmitSuccess()
                            navController.popBackStack()
                        },
                        onDismiss = {
                            reviewViewModel.resetSubmitSuccess()
                            navController.popBackStack()
                        }
                    )
                }
                composable(AppScreen.AdminViewReview.name) {
                    if (currentAdminDept != "Loading...") {
                        AdminReviewScreen(
                            adminDepartment = currentAdminDept,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        // Optional: Show a progress bar while waiting for the department name
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }

            }
        }
    }
}
