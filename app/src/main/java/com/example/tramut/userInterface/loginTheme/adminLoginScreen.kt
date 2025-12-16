package com.example.tramut.userInterface.loginTheme

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myfacilitybookingsystem.viewModel.AdminsViewModel
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.ErrorRed

@Composable
fun AdminLoginScreen(
    viewModel: AdminsViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var adminId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val isError = viewModel.loginError.value

    // FIX: Only show Red X if error exists. No green check while typing.
    val idValidationState = if (isError) false else null

    AdminLoginScreen(
        adminId = adminId,
        onAdminIdChange = { adminId = it },
        idValid = idValidationState,
        password = password,
        onPasswordChange = { password = it },
        showLoginError = isError,
        onLoginClick = {
            if (adminId.isBlank() || password.isBlank()) {
                Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.login(
                    loginId = adminId,
                    pass = password,
                    onSuccess = {
                        Toast.makeText(context, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess()
                    }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminLoginScreen(
    adminId: String,
    onAdminIdChange: (String) -> Unit,
    idValid: Boolean?,
    password: String,
    onPasswordChange: (String) -> Unit,
    showLoginError: Boolean,
    onLoginClick: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().background(Background).padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.tarumt),
            contentDescription = "Logo",
            modifier = Modifier.heightIn(350.dp).align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            label = { Text("Admin ID") },
            value = adminId,
            onValueChange = onAdminIdChange,
            leadingIcon = { Icon(Icons.Outlined.Person, "ID Icon", tint = Color.Black) },
            trailingIcon = {
                when (idValid) {
                    true -> Icon(Icons.Default.Check, "Valid", tint = Color.Green)
                    false -> Icon(Icons.Default.Close, "Invalid", tint = Color.Red)
                    null -> {}
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            label = { Text("Password") },
            value = password,
            onValueChange = onPasswordChange,
            leadingIcon = { Icon(Icons.Outlined.Lock, "Lock Icon", tint = Color.Black) },
            trailingIcon = {
                val iconRes = if (showPassword) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(painter = painterResource(id = iconRes), contentDescription = "Toggle", modifier = Modifier.size(28.dp))
                }
            },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (showLoginError) {
            Text("Invalid login ID or password.", color = ErrorRed, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onLoginClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
            modifier = Modifier.fillMaxWidth().height(57.dp),
            shape = RoundedCornerShape(20.dp)
        ) { Text("Login", fontSize = 18.sp, color = Color.White) }
    }
}