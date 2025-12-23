package com.example.tramut.userInterface.loginTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.BlueMain
import com.example.tramut.ui.theme.ErrorRed
import com.example.tramut.ui.theme.ForgotBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLoginScreen(
    studentId: String,
    onStudIdChange: (String) -> Unit,
    idValid: Boolean?,
    password: String,
    onPasswordChange: (String) -> Unit,
    showLoginError: Boolean,
    onLoginClick: () -> Unit,
    onForgotPassClick: () -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.tarumt),
            contentDescription = "Logo",
            modifier = Modifier
                .heightIn(350.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Student ID
        OutlinedTextField(
            label = { Text("Student ID") },
            value = studentId,
            onValueChange = onStudIdChange,
            leadingIcon = {
                Icon(Icons.Outlined.Person,
                    contentDescription = "ID Icon",
                    tint = MaterialTheme.colorScheme.onBackground)
            },
            trailingIcon = {
                when (idValid) {
                    true -> Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Valid",
                        tint = Color.Green
                    )
                    false -> Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Invalid",
                        tint = Color.Red
                    )
                    null -> {}
                }
            },
            singleLine = true,
            isError = false,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Password
        OutlinedTextField(
            label = { Text("Password") },
            value = password,
            onValueChange = onPasswordChange,
            leadingIcon = {
                Icon(Icons.Outlined.Lock,
                    contentDescription = "Lock Icon",
                    tint = MaterialTheme.colorScheme.onBackground)
            },
            trailingIcon = {
                val iconRes = if (showPassword) R.drawable.ic_visibility else R.drawable.ic_visibility_off
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = "Toggle Password",
                        modifier = Modifier
                            .size(28.dp)
                    )
                }
            },
            singleLine = true,
            isError = showLoginError,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        if (showLoginError) {
            Text(
                text = "Invalid login ID or password.",
                color = ErrorRed,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onLoginClick,
            enabled = studentId.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = BlueMain),
            modifier = Modifier
                .fillMaxWidth()
                .height(57.dp),
            shape = RoundedCornerShape(20.dp)
        ) { Text("Login", fontSize = 18.sp, color = Color.White) }

        Spacer(modifier = Modifier.height(15.dp))

        Text(
            text = "Forgot Password?",
            color = ForgotBlue,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onForgotPassClick() }
        )
    }
}
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    StudentLoginScreen(
        showLoginError = false,
        onLoginClick = {},
        onForgotPassClick = {},
        studentId = "",
        onStudIdChange = {},
        password = "",
        onPasswordChange = {},
        idValid = null
    )
}