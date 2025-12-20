package com.example.tramut.userInterface.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.GrayText
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.PasswordRule
import com.example.tramut.userInterface.loginTheme.forgotPwdTheme.SmallPasswordField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    errorMsg: String?,
    oldPassword: String,
    onOldPasswordChange: (String) -> Unit,
    newPassword: String,
    confirmPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onChangePasswordClick: () -> Unit,
    onCancelClick: () -> Unit,
    ruleMinLength: Boolean,
    ruleLower: Boolean,
    ruleUpper: Boolean,
    ruleNumberSpecial: Boolean
)
{
    val isPasswordValid = ruleMinLength && ruleLower && ruleUpper && ruleNumberSpecial
    val passwordsMatch = newPassword == confirmPassword
    val isSubmitEnabled = isPasswordValid && passwordsMatch && newPassword.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(horizontal = 34.dp)
    ) {

        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = "Follow the password rule",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))


        PasswordRule(
            text = "Password must be at least 8 characters long.",
            satisfied = ruleMinLength
        )

        PasswordRule(
            text = "Password must contain at least one lower case.",
            satisfied = ruleLower
        )

        PasswordRule(
            text = "Password must contain at least one upper case.",
            satisfied = ruleUpper
        )

        PasswordRule(
            text = "Password must contain at least one number or special character.",
            satisfied = ruleNumberSpecial
        )

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Old Password",
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier
                .padding(bottom = 4.dp)
        )

        SmallPasswordField(
            value = oldPassword,
            onValueChange = onOldPasswordChange,
            isError = false
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "New Password",
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier
                .padding(bottom = 4.dp)
        )

        SmallPasswordField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            isError = newPassword.isNotEmpty() && !isPasswordValid
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Confirm Password",
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier
                .padding(bottom = 4.dp)
        )

        SmallPasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
        )

        Spacer(modifier = Modifier.height(20.dp))


        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
            Text(
                text = "Password does not match",
                fontSize = 12.sp,
                color = Color.Red
            )
        }else if (errorMsg != null) {
            Text(
                text = errorMsg,
                fontSize = 12.sp,
                color = Color.Red
            )
        }


        Spacer(modifier = Modifier.height(28.dp))


        Button(
            onClick = onChangePasswordClick,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            enabled = isSubmitEnabled,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(text = "Submit", color = Color.White, fontSize = 14.sp)
        }


        Spacer(modifier = Modifier.height(14.dp))


        OutlinedButton(
            onClick = onCancelClick ,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
        ) {
            Text(
                text = "Cancel",
                color = Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Preview (showBackground = true, showSystemUi = true)
@Composable
fun Pre() {
    ChangePasswordScreen(
        errorMsg = null,
        newPassword = "",
        confirmPassword = "",
        oldPassword = "",
        onChangePasswordClick = {},
        onCancelClick = {},
        onOldPasswordChange = {},
        onNewPasswordChange = {},
        onConfirmPasswordChange = {},
        ruleMinLength = true,
        ruleLower = true,
        ruleUpper = true,
        ruleNumberSpecial = true
    )
}
