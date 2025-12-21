package com.example.tramut.userInterface.loginTheme.forgotPwdTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tramut.R
import com.example.tramut.ui.theme.Background
import com.example.tramut.ui.theme.GrayText
import kotlinx.coroutines.delay


@Composable
fun ForgetPasswordScreen3 (
    email: String?,
    oobCode: String,
    onOobCodeChange: (String) -> Unit,
    onResendCodeClick: () -> Unit,
    onContinueResetClick: () -> Unit,
    onChangeEmailClick: () -> Unit
) {

    var timeLeft by remember { mutableStateOf(120) }

    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    val canResend = timeLeft == 0

    val maskedEmail = remember(email) {
        maskEmail(email)
    }
    Column(
        modifier = Modifier
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
            text = "Retrieve Password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You will receive an email with a verification code to reset your password. Please check your inbox.\n\nEnter oobCode sent to $maskedEmail",
            fontSize = 13.sp,
            color = GrayText
        )
        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = oobCode,
            onValueChange = onOobCodeChange,
            placeholder = {
                Text(
                    text = "oobCode",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
                          },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true
        )
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


        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = onContinueResetClick,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            enabled = oobCode.isNotEmpty(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                text = "Continue Reset Password",
                color = Color.White,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "*Please make sure the verification code is entered correctly. ",
            fontSize = 12.sp,
            color = GrayText
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Change Email Address",
            color = Color.Black,
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onChangeEmailClick() }
        )
    }
}

fun maskEmail(email: String?): String? {
    val parts = email?.split("@")
    if (parts?.size != 2) return email

    val name = parts[0]
    val domain = parts[1]

    if (name.length <= 2) {
        return "${name.first()}****@$domain"
    }

    return "${name.first()}****${name.last()}@$domain"
}
@Preview (showBackground = true)
@Composable
fun FGPW3() {
    ForgetPasswordScreen3(
        email = "p0934@tarc.edu.my",
        oobCode = "",
        onOobCodeChange = {},
        onResendCodeClick = {},
        onContinueResetClick = {},
        onChangeEmailClick = {}
    )
}