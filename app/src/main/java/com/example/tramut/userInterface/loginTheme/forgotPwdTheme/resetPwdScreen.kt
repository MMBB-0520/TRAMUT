package com.example.tramut.userInterface.loginTheme.forgotPwdTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.GrayText

@Composable
fun ResetPasswordScreen (
    newPassword: String,
    confirmPassword: String,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onCancelClick: () -> Unit,
    ruleMinLength: Boolean,
    ruleLower: Boolean,
    ruleUpper: Boolean,
    ruleNumberSpecial: Boolean
) {
    val isPasswordValid = ruleMinLength && ruleLower && ruleUpper && ruleNumberSpecial
    val passwordsMatch = newPassword == confirmPassword
    val isSubmitEnabled = isPasswordValid && passwordsMatch && newPassword.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 34.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Image(
            painter = painterResource(id = R.drawable.tarumt),
            contentDescription = "Logo",
            modifier = Modifier
                .size(386.dp, 232.dp)
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "New Credentials",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
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
            text = "New Password",
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier
                .padding(bottom = 4.dp)
        )

        SmallPasswordField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            isError = newPassword.isNotEmpty() && !isPasswordValid // 如果输入了但不满足规则
        )

        Spacer(modifier = Modifier.height(24.dp))

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

        Spacer(modifier = Modifier.height(24.dp))


        if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
            Text(
                text = "Password does not match",
                fontSize = 12.sp,
                color = Color.Red
            )
        }


        Spacer(modifier = Modifier.height(28.dp))


        Button(
            onClick = onSubmitClick,
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
@Composable
fun PasswordRule(
    text: String,
    satisfied: Boolean
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(
                id = if (satisfied)
                    R.drawable.ic_correct
                else
                    R.drawable.ic_wrong
            ),            contentDescription = null,
            tint = if (satisfied) Color(0xFF4CF50A) else Color.Red,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 13.sp, color = Color.DarkGray)
    }
}
@Composable
fun SmallPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    val borderColor = when {
        isError -> Color.Red
        value.isNotEmpty() -> Color.Black
        else -> Color.Black
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = Color.Black
            )
        )
    }
}
@Preview (showBackground = true)
@Composable
fun ResetPasswordPreview() {
    ResetPasswordScreen(
        newPassword = "",
        confirmPassword = "",
        onNewPasswordChange = {},
        onConfirmPasswordChange = {},
        ruleMinLength = true,
        ruleLower = true,
        ruleUpper = true,
        ruleNumberSpecial = true,
        onSubmitClick = {},
        onCancelClick = {}

    )
}