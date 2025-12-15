package com.example.tramut.userInterface.loginTheme.forgotPwdTheme

import android.R.attr.enabled
import android.R.attr.onClick
import android.R.attr.singleLine
import android.R.attr.text
import android.R.attr.textStyle
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation.Companion.keyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import kotlinx.coroutines.delay
import java.time.format.TextStyle
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun ForgetPasswordScreen2 (
    email: String,
    otpValues: List<String> = List(6) { "" },
    onOtpChange: (Int, String) -> Unit = { _, _ -> },
    onVerifyClick: () -> Unit = {},
    onResendCodeClick: () -> Unit = {},
    onChangeEmailClick: () -> Unit = {}
) {
    val otpValues = remember {
        mutableStateListOf("", "", "", "", "", "")
    }

    val focusRequesters = remember {
        List(6) { FocusRequester() }
    }

    val isOtpComplete = otpValues.all { it.length == 1 }

    var timeLeft by remember { mutableStateOf(120) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    val canResend = timeLeft == 0


    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Column(
        modifier = Modifier
            .background(Background)
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
            text = "Code Verification",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You will receive an email with a verification code to reset your password. Please check your inbox.\n\nEnter OTP sent to $email",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(38.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            otpValues.forEachIndexed { index, value ->

                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->

                        // 粘贴情况
                        if (newValue.length > 1) {

                            val digits = newValue.filter { it.isDigit() }

                            digits.forEachIndexed { i, char ->
                                if (index + i < 6) {
                                    otpValues[index + i] = char.toString()
                                }
                            }

                            val lastIndex = (index + digits.length - 1).coerceAtMost(5)
                            focusRequesters[lastIndex].requestFocus()
                            return@OutlinedTextField
                        }

                        // 单个输入
                        if (newValue.all { it.isDigit() }) {

                            otpValues[index] = newValue

                            when {
                                newValue.length == 1 && index < 5 ->
                                    focusRequesters[index + 1].requestFocus()

                                newValue.isEmpty() && index > 0 ->
                                    focusRequesters[index - 1].requestFocus()
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(45.dp)
                        .focusRequester(focusRequesters[index])
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = if (canResend)
                "Resend"
            else
                "Resend ($timeLeft s)",
            color = if (canResend)
                Color(0xFF1976D2)
            else
                Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .clickable(
                    enabled = canResend
                ) {
                    onResendCodeClick()
                }
        )

        Spacer(modifier = Modifier.height(45.dp))

        Button(
            onClick = onVerifyClick,
            enabled = isOtpComplete,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOtpComplete) Color.Black else Color.Gray
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Verify Code", color = Color.White, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Change Email Address",
            color = Color.Black,
            fontSize = 17.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onChangeEmailClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewForgetPasswordScreen2() {
    ForgetPasswordScreen2(
        onResendCodeClick = {},
        onChangeEmailClick = {},
        email = "william.henry.harrison@example-pet-store.com",
        otpValues = List(6) { "" },
        onVerifyClick = {},
        onOtpChange = { _, _ -> }
    )
}