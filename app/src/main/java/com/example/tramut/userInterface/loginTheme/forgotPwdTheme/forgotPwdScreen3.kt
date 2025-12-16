package com.example.tramut.userInterface.loginTheme.forgotPwdTheme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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


@Composable
fun ForgetPasswordScreen3 (
    onResendCodeClick: () -> Unit,
    onChangeEmailClick: () -> Unit
) {

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
            text = "Retrieve Password",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "You will receive an email with a verification code to reset your password. Please check your inbox.",
            fontSize = 13.sp,
            color = Color.Gray
        )


        Spacer(modifier = Modifier.height(50.dp))

        Button(
            onClick = onResendCodeClick,
            modifier = Modifier
                .width(287.dp)
                .height(43.dp)
                .align(Alignment.CenterHorizontally),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(
                text = "Resend Verification Code",
                color = Color.White,
                fontSize = 14.sp
            )
        }

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

@Preview (showBackground = true)
@Composable
fun FGPW3() {
    ForgetPasswordScreen3(
        onResendCodeClick = {},
        onChangeEmailClick = {}
    )
}